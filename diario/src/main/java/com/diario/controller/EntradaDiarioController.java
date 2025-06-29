package com.diario.controller;

import com.diario.dto.EntradaDiarioResponseDTO;
import com.diario.model.EntradaDiario;
import com.diario.model.Usuario;
import com.diario.service.EntradaDiarioService;
import com.diario.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entradas")
public class EntradaDiarioController {

    private static final Logger logger = LoggerFactory.getLogger(EntradaDiarioController.class);

    @Autowired
    private EntradaDiarioService entradaDiarioService;

    @Autowired
    private UsuarioService usuarioService;

    // Injeta o diretório de upload do application.properties:
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Endpoint para servir arquivos estáticos (imagens, vídeos, áudios, documentos).
     *
     * @param nomeArquivo O nome do arquivo a ser recuperado.
     * @return ResponseEntity contendo os bytes do arquivo e o tipo de mídia.
     * @throws IOException Se houver um erro ao ler o arquivo.
     */
    @GetMapping("/arquivo/{nomeArquivo}")
    public ResponseEntity<byte[]> getArquivo(@PathVariable String nomeArquivo) throws IOException {
        Path filePath = Paths.get(uploadDir, nomeArquivo);
        logger.info("Tentando servir arquivo: {}", filePath);

        if (!Files.exists(filePath)) {
            logger.warn("Arquivo não encontrado: {}", filePath);
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = Files.readAllBytes(filePath);

        String mimeType = Files.probeContentType(filePath);
        logger.info("MIME Type detectado por Files.probeContentType: {}", mimeType);

        if (mimeType == null) {
            String fileExtension = "";
            int dotIndex = nomeArquivo.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < nomeArquivo.length() - 1) {
                fileExtension = nomeArquivo.substring(dotIndex + 1).toLowerCase();
            }

            switch (fileExtension) {
                case "pdf":
                    mimeType = "application/pdf";
                    break;
                case "jpg":
                case "jpeg":
                    mimeType = "image/jpeg";
                    break;
                case "png":
                    mimeType = "image/png";
                    break;
                case "gif":
                    mimeType = "image/gif";
                    break;
                case "mp4":
                    mimeType = "video/mp4";
                    break;
                case "webm":
                    mimeType = "video/webm";
                    break;
                case "mp3":
                    mimeType = "audio/mpeg";
                    break;
                case "wav":
                    mimeType = "audio/wav";
                    break;
                case "doc":
                    mimeType = "application/msword";
                    break;
                case "docx":
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    break;
                case "xls":
                    mimeType = "application/vnd.ms-excel";
                    break;
                case "xlsx":
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                default:
                    mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                    break;
            }

            logger.info("MIME Type inferido pela extensão (fallback): {}", mimeType);
        }

        logger.info("MIME Type FINAL enviado na resposta: {}", mimeType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header("Content-Disposition", "inline; filename=\"" + nomeArquivo + "\"")
                .body(fileBytes);
    }

    /**
     * Endpoint para listar todas as entradas do diário de um usuário.
     * Requer autenticação (Principal).
     *
     * @param principal Objeto Principal que representa o usuário autenticado.
     * @return Lista de DTOs das entradas do diário do usuário.
     */
    @GetMapping
    public ResponseEntity<List<EntradaDiarioResponseDTO>> listarEntradas(Principal principal) {
        String username = principal.getName();

        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<EntradaDiario> entradas = entradaDiarioService.listarEntradasPorUsuario(usuario.getId());

        // Converte as entidades para DTOs pra não mostrar detalhes internos do modelo
        List<EntradaDiarioResponseDTO> dtos = entradas.stream()
                .map(EntradaDiarioResponseDTO::new)
                .toList();

        // Retorna a lista de DTOs com cabeçalhos para controle de cache
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(dtos);
    }

    /**
     * Endpoint para criar uma nova entrada no diário com suporte a múltiplos arquivos de mídia.
     *
     * @param titulo    O título  da entrada.
     * @param conteudo  O conteúdo textual da entrada.
     * @param arquivos  Lista de arquivos de mídia (imagem, vídeo, áudio, documento) a serem anexados.
     * @param principal Objeto Principal que representa o usuário autenticado.
     * @return A entrada do diário salva, incluindo as mídias anexadas, como DTO.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntradaDiarioResponseDTO> criarEntradaComMidias(
            @RequestPart("titulo") String titulo,
            @RequestPart("conteudo") String conteudo,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos,
            Principal principal) {

        String username = principal.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EntradaDiario entrada = new EntradaDiario();
        entrada.setTitulo(titulo);
        entrada.setConteudo(conteudo);
        entrada.setData(LocalDate.now());
        entrada.setUsuario(usuario);

        // Salva a entrada principal primeiro para ganhar um ID
        EntradaDiario entradaSalva = entradaDiarioService.criarEntrada(entrada, usuario.getId());

        // Se tiver arquivos, processa e anexa
        if (arquivos != null && !arquivos.isEmpty()) {
            for (MultipartFile file : arquivos) {
                if (!file.isEmpty()) {
                    try {
                        String tipoArquivo = inferirTipoArquivo(file); // Método auxiliar
                        entradaDiarioService.anexarMidiaAEntrada(entradaSalva.getId(), file, tipoArquivo);
                    } catch (RuntimeException e) {
                        System.err.println("Erro ao anexar arquivo " + file.getOriginalFilename() + " para a entrada " + entradaSalva.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
        // Tem que buscar a entrada novamente pra garantir que a lista 'midias' esteja populada
        // E converter para DTO antes de retornar
        return ResponseEntity.ok(entradaDiarioService.buscarPorId(entradaSalva.getId())
                .map(EntradaDiarioResponseDTO::new) // Converte para DTO
                .orElseThrow(() -> new RuntimeException("Erro ao buscar entrada recém-criada para serialização.")));
    }

    /**
     * Endpoint para buscar uma entrada específica por ID.
     *
     * @param id O ID da entrada a ser buscada.
     * @return A entrada do diário se encontrada, ou 404 Not Found, como DTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntradaDiarioResponseDTO> buscarPorId(@PathVariable Long id) { // Alterado para retornar DTO
        Optional<EntradaDiario> entrada = entradaDiarioService.buscarPorId(id);
        // Mapeia a entidade pra DTO antes de retornar
        return entrada.map(EntradaDiarioResponseDTO::new) // Converte para DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para atualizar uma entrada existente no diário, com suporte a adição/remoção de mídias.
     *
     * @param id               O ID da entrada a ser atualizada.
     * @param titulo           O novo título da entrada.
     * @param conteudo         O novo conteúdo da entrada.
     * @param novosArquivos    Opcional: lista de novos arquivos de mídia a serem adicionados.
     * @param idsMidiasRemover Opcional: lista de IDs de mídias existentes a serem removidas.
     * @param principal        Objeto Principal que representa o usuário autenticado.
     * @return A entrada do diário atualizada, incluindo as mídias, como DTO.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntradaDiarioResponseDTO> atualizarEntradaComMidias(
            @PathVariable Long id,
            @RequestPart("titulo") String titulo,
            @RequestPart("conteudo") String conteudo,
            @RequestPart(value = "novosArquivos", required = false) List<MultipartFile> novosArquivos,
            @RequestPart(value = "idsMidiasRemover", required = false) List<Long> idsMidiasRemover,
            Principal principal) {

        String username = principal.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EntradaDiario entradaExistente = entradaDiarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Entrada não encontrada com o ID: " + id));

        // Verifica se o usuário autenticado é o dono da entrada
        if (!entradaExistente.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }

        entradaExistente.setTitulo(titulo);
        entradaExistente.setConteudo(conteudo);

        // 1. Remove mídias existentes
        if (idsMidiasRemover != null && !idsMidiasRemover.isEmpty()) {
            for (Long midiaId : idsMidiasRemover) {
                try {
                    entradaDiarioService.removerMidiaDaEntrada(midiaId, id); // Passa o ID da entrada pra validação
                } catch (RuntimeException e) {
                    System.err.println("Erro ao remover mídia com ID " + midiaId + ": " + e.getMessage());
                }
            }
        }

        // 2. Adicionar novas mídias
        if (novosArquivos != null && !novosArquivos.isEmpty()) {
            for (MultipartFile file : novosArquivos) {
                if (!file.isEmpty()) {
                    try {
                        String tipoArquivo = inferirTipoArquivo(file);
                        entradaDiarioService.anexarMidiaAEntrada(entradaExistente.getId(), file, tipoArquivo);
                    } catch (RuntimeException e) {
                        System.err.println("Erro ao anexar novo arquivo " + file.getOriginalFilename() + ": " + e.getMessage());
                    }
                }
            }
        }

        // Salva as alterações na EntradaDiario. As mídias já foram tratadas lá no service.
        EntradaDiario atualizada = entradaDiarioService.atualizarEntrada(entradaExistente);

        // Tem que buscar a entrada novamente para garantir que a lista 'midias' esteja populada
        // E converter para DTO antes de retornar
        return ResponseEntity.ok(entradaDiarioService.buscarPorId(atualizada.getId())
                .map(EntradaDiarioResponseDTO::new) // Converte para DTO
                .orElseThrow(() -> new RuntimeException("Erro ao buscar entrada atualizada para serialização.")));
    }

    /**
     * Endpoint para deletar uma entrada do diário por ID.
     * Verifica se o usuário autenticado é o proprietário da entrada antes de deletar.
     *
     * @param id        O ID da entrada a ser deletada.
     * @param principal Objeto Principal que representa o usuário autenticado.
     * @return Resposta sem conteúdo (204 No Content) se a deleção for bem-sucedida,
     * 404 Not Found se a entrada não existir, ou 403 Forbidden se o usuário não for o dono.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEntrada(@PathVariable Long id, Principal principal) {
        Optional<EntradaDiario> entradaOpt = entradaDiarioService.buscarPorId(id);

        if (entradaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EntradaDiario entrada = entradaOpt.get();

        String username = principal.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!entrada.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden, meu chapa
        }

        entradaDiarioService.deletarEntrada(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Método auxiliar para inferir o tipo de arquivo com base no Content-Type.
     *
     * @param file O MultipartFile.
     * @return Uma string representando o tipo de arquivo (ex: "imagem", "video", "documento_pdf").
     */
    private String inferirTipoArquivo(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "desconhecido";
        }
        if (contentType.startsWith("image/")) {
            return "imagem";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else if (contentType.equals("application/pdf")) {
            return "documento_pdf";
        } else if (contentType.equals("application/msword") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "documento_word";
        }
        // Dá pra meter mais tipos MIME depois
        return "outro_documento";
    }
}
package com.diario.service;

import com.diario.model.EntradaDiario;
import com.diario.model.MidiaEntrada;
import com.diario.model.Usuario;
import com.diario.repository.EntradaDiarioRepository;
import com.diario.repository.MidiaEntradaRepository;
import com.diario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EntradaDiarioService {

    @Autowired
    private EntradaDiarioRepository entradaDiarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MidiaEntradaRepository midiaEntradaRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Lista todas as entradas do diário associadas a um usuário específico.
     * @param usuarioId O ID do usuário cujas entradas devem ser listadas.
     * @return Uma lista de objetos EntradaDiario.
     */
    public List<EntradaDiario> listarEntradasPorUsuario(Long usuarioId) {
        return entradaDiarioRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Cria uma nova entrada no diário para um usuário.
     * @param entradaDiario O objeto EntradaDiario a ser criado.
     * @param usuarioId O ID do usuário ao qual a entrada será associada.
     * @return A EntradaDiario salva após a operação.
     * @throws RuntimeException se o usuário não for encontrado.
     */
    @Transactional // Transactional garante que todas as operações relacionadas ao salvamento
    // e anexação de mídias tão juntos na mesma transação
    public EntradaDiario criarEntrada(EntradaDiario entradaDiario, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        entradaDiario.setUsuario(usuario);
        return entradaDiarioRepository.save(entradaDiario);
    }

    /**
     * Busca uma entrada do diário pelo seu ID.
     * @param id O ID da entrada a ser buscada.
     * @return Um Optional contendo a EntradaDiario se encontrada, ou um Optional vazio.
     */
    @Transactional(readOnly = true) // Garante que as mídias podem ser carregadas em Lazy
    public Optional<EntradaDiario> buscarPorId(Long id) {
        return entradaDiarioRepository.findByIdWithMidias(id);
    }


    /**
     * Deleta uma entrada do diário pelo seu ID.
     * Antes de deletar a entrada do DB, apaga os arquivos físicos associados.
     * @param id O ID da entrada a ser deletada.
     */
    @Transactional
    public void deletarEntrada(Long id) {
        EntradaDiario entrada = entradaDiarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrada de diário não encontrada com ID: " + id));

        // Apaga todos os arquivos físicos associados a esta entrada
        entrada.getMidias().forEach(midia -> {
            try {
                apagarArquivoFisico(midia.getCaminhoArquivo());
            } catch (IOException e) {
                System.err.println("Aviso: Não foi possível apagar o arquivo físico da mídia " + midia.getCaminhoArquivo() + ": " + e.getMessage());
            }
        });

        entradaDiarioRepository.deleteById(id);
    }

    /**
     * Atualiza uma entrada do diário existente.
     * @param entradaDiario O objeto EntradaDiario com os dados atualizados (deve conter um ID existente).
     * @return A EntradaDiario atualizada e salva no banco de dados.
     */
    @Transactional
    public EntradaDiario atualizarEntrada(EntradaDiario entradaDiario) {
        return entradaDiarioRepository.save(entradaDiario);
    }

    /**
     * Anexa uma mídia (imagem, vídeo, áudio, documento) a uma entrada de diário existente.
     * Salva o arquivo no sistema de arquivos e os metadados no banco de dados.
     *
     * @param entradaId O ID da entrada de diário à qual a mídia será anexada.
     * @param file O arquivo MultipartFile recebido da requisição.
     * @param tipoArquivo O tipo de arquivo (ex: "video", "audio", "documento_pdf", "imagem").
     * @return O objeto MidiaEntrada salvo no banco de dados.
     * @throws RuntimeException se a entrada de diário não for encontrada ou ocorrer um erro no upload.
     */
    @Transactional
    public MidiaEntrada anexarMidiaAEntrada(Long entradaId, MultipartFile file, String tipoArquivo) {
        EntradaDiario entrada = entradaDiarioRepository.findByIdWithMidias(entradaId) // findByIdWithMidias garante que as mídias existentes sejam carregadas
                .orElseThrow(() -> new RuntimeException("Entrada de diário não encontrada com ID: " + entradaId));

        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio. Por favor, selecione um arquivo para upload.");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            MidiaEntrada midia = new MidiaEntrada(
                    entrada,
                    tipoArquivo,
                    filePath.toString(),
                    originalFileName
            );

            // Adiciona a mídia à lista da entrada e define a relação bidirecional
            entrada.addMidia(midia);

            return midiaEntradaRepository.save(midia);

        } catch (IOException ex) {
            throw new RuntimeException("Falha ao armazenar o arquivo " + file.getOriginalFilename() + ". Por favor, tente novamente!", ex);
        }
    }

    /**
     * Remove uma mídia específica de uma entrada de diário.
     * Deleta o arquivo físico e o registro no banco de dados.
     * @param midiaId O ID da mídia a ser removida.
     * @param entradaId O ID da entrada de diário à qual a mídia pertence (para validação de segurança).
     * @throws RuntimeException se a mídia não for encontrada ou não pertencer à entrada especificada, ou erro ao apagar o arquivo.
     */
    @Transactional
    public void removerMidiaDaEntrada(Long midiaId, Long entradaId) {
        // Carrega a entrada com as mídias pra garantir que a coleção 'midias' seja inicializada
        EntradaDiario entrada = entradaDiarioRepository.findByIdWithMidias(entradaId)
                .orElseThrow(() -> new RuntimeException("Entrada de diário não encontrada com ID: " + entradaId));

        MidiaEntrada midia = midiaEntradaRepository.findById(midiaId)
                .orElseThrow(() -> new RuntimeException("Mídia não encontrada com ID: " + midiaId));

        if (!midia.getEntradaDiario().getId().equals(entrada.getId())) { // Comparar com a entrada recarregada
            throw new RuntimeException("Mídia não pertence à entrada de diário especificada.");
        }

        try {
            apagarArquivoFisico(midia.getCaminhoArquivo());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao apagar o arquivo físico da mídia " + midia.getNomeOriginalArquivo() + ": " + e.getMessage(), e);
        }

        // Remove a mídia da lista da EntradaDiario para manter a consistência do modelo em memória
        entrada.removeMidia(midia);

        midiaEntradaRepository.delete(midia);
    }

    /**
     * Método auxiliar para apagar um arquivo do disco.
     * @param caminhoCompletoArquivo O caminho completo do arquivo a ser apagado.
     * @throws IOException Se ocorrer um erro durante a operação de E/S.
     */
    private void apagarArquivoFisico(String caminhoCompletoArquivo) throws IOException {
        Path filePath = Paths.get(caminhoCompletoArquivo);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}

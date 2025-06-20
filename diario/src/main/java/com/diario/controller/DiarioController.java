package com.diario.controller;

import com.diario.model.EntradaDiario;
import com.diario.service.EntradaDiarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciar operações relacionadas a entradas de diário.
 * Este controlador lida com requisições HTTP para listar, criar e deletar entradas de diário.
 */
@RestController
@RequestMapping("/diario")
public class DiarioController {

    @Autowired
    private EntradaDiarioService entradaDiarioService;

    /**
     * Endpoint GET para listar todas as entradas de diário de um usuário específico.
     *
     * @param usuarioId O ID do usuário cujas entradas serão listadas.
     * @return ResponseEntity contendo uma lista de EntradaDiario se encontradas,
     * ou uma resposta HTTP apropriada caso contrário.
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<EntradaDiario>> getEntradas(@PathVariable Long usuarioId) {
        List<EntradaDiario> entradas = entradaDiarioService.listarEntradasPorUsuario(usuarioId);
        return ResponseEntity.ok(entradas);
    }

    /**
     * Endpoint POST para criar uma nova entrada de diário para um usuário específico.
     *
     * @param usuarioId O ID do usuário ao qual a nova entrada será associada.
     * @param entradaDiario O objeto EntradaDiario contendo os dados da nova entrada a ser criada.
     * @return ResponseEntity contendo a EntradaDiario recém-criada.
     */
    @PostMapping("/{usuarioId}")
    public ResponseEntity<EntradaDiario> createEntrada(@PathVariable Long usuarioId,
                                                       @RequestBody EntradaDiario entradaDiario) {
        EntradaDiario novaEntrada = entradaDiarioService.criarEntrada(entradaDiario, usuarioId);
        return ResponseEntity.ok(novaEntrada);
    }

    /**
     * Endpoint DELETE para remover uma entrada de diário pelo seu ID.
     *
     * @param id O ID da entrada de diário a ser deletada.
     * @return ResponseEntity com status 204 No Content se a deleção for bem-sucedida.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntrada(@PathVariable Long id) {
        entradaDiarioService.deletarEntrada(id);
        return ResponseEntity.noContent().build();
    }
}

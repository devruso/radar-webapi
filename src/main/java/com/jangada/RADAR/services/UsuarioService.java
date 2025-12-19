package com.jangada.RADAR.services;

import java.util.Set;

import com.jangada.RADAR.models.dtos.AtualizarDisciplinasDTO;
import com.jangada.RADAR.models.dtos.AtualizarTurnosDTO;
import com.jangada.RADAR.models.dtos.BanirProfessorDTO;
import com.jangada.RADAR.models.dtos.LoginDTO;
import com.jangada.RADAR.models.dtos.RegisterDTO;
import com.jangada.RADAR.models.dtos.UsuarioCadastroDTO;
import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.dtos.UsuarioTesteDTO;

/**
 * Serviço de gerenciamento de usuários do RADAR.
 */
public interface UsuarioService {
    
    /**
     * Cria um usuário teste (sem cadastro completo).
     * Permite uso da aplicação sem login.
     */
    UsuarioDTO criarUsuarioTeste(UsuarioTesteDTO dto);
    
    /**
     * Cadastra um novo usuário com email e senha.
     * Valida se senhas coincidem e se email já existe.
     */
    UsuarioDTO cadastrarUsuario(UsuarioCadastroDTO dto);
    
    /**
     * Atualiza as disciplinas concluídas pelo usuário.
     * Usado na tela de seleção de disciplinas por semestre.
     */
    UsuarioDTO atualizarDisciplinas(Long usuarioId, AtualizarDisciplinasDTO dto);
    
    /**
     * Atualiza os turnos disponíveis do usuário (matutino, vespertino, noturno).
     */
    UsuarioDTO atualizarTurnos(Long usuarioId, AtualizarTurnosDTO dto);
    
    /**
     * Adiciona um professor à lista de banidos do usuário.
     */
    UsuarioDTO banirProfessor(Long usuarioId, BanirProfessorDTO dto);
    
    /**
     * Remove um professor da lista de banidos do usuário.
     */
    UsuarioDTO desbanirProfessor(Long usuarioId, BanirProfessorDTO dto);
    
    /**
     * Lista todos os professores banidos pelo usuário.
     */
    Set<String> listarProfessoresBanidos(Long usuarioId);
    
    /**
     * Autentica usuário via email e senha.
     * @throws ResourceNotFoundException se credenciais inválidas
     */
    UsuarioDTO login(LoginDTO dto);
    
    /**
     * Registra novo usuário simples (apenas nome, email, senha).
     * @throws IllegalStateException se email já existe
     */
    UsuarioDTO register(RegisterDTO dto);

}

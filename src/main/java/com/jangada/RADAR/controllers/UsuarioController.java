package com.jangada.RADAR.controllers;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.UsuarioMapper;
import com.jangada.RADAR.models.dtos.AtualizarDisciplinasDTO;
import com.jangada.RADAR.models.dtos.AtualizarPerfilDTO;
import com.jangada.RADAR.models.dtos.AtualizarTurnosDTO;
import com.jangada.RADAR.models.dtos.AuthResponseDTO;
import com.jangada.RADAR.models.dtos.BanirProfessorDTO;
import com.jangada.RADAR.models.dtos.LoginDTO;
import com.jangada.RADAR.models.dtos.UsuarioCadastroDTO;
import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.dtos.UsuarioTesteDTO;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.services.AuthTokenService;
import com.jangada.RADAR.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Cadastro, autenticação e preferências do estudante")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final AuthTokenService authTokenService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            AuthTokenService authTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.authTokenService = authTokenService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> getOne(@PathVariable Long id) {
        return usuarioRepository.findByIdWithDetails(id)
                .map(usuario -> ResponseEntity.ok(UsuarioMapper.toDto(usuario)))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @PostMapping("/teste")
    @Operation(summary = "Cria uma sessão temporária sem cadastro")
    public ResponseEntity<AuthResponseDTO> criarTeste(@Valid @RequestBody UsuarioTesteDTO dto) {
        return ResponseEntity.ok(authTokenService.issue(usuarioService.criarUsuarioTeste(dto)));
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra e autentica um estudante")
    public ResponseEntity<AuthResponseDTO> cadastrar(@Valid @RequestBody UsuarioCadastroDTO dto) {
        return ResponseEntity.ok(authTokenService.issue(usuarioService.cadastrarUsuario(dto)));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica por email e senha")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authTokenService.issue(usuarioService.login(dto)));
    }

    @PostMapping("/{id}/disciplinas")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> atualizarDisciplinas(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarDisciplinasDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarDisciplinas(id, dto));
    }

    @PostMapping("/{id}/turnos")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> atualizarTurnos(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTurnosDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarTurnos(id, dto));
    }

    @PostMapping("/{id}/perfil")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> atualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPerfilDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(id, dto));
    }

    @PostMapping("/{id}/professores/banir")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> banirProfessor(
            @PathVariable Long id,
            @Valid @RequestBody BanirProfessorDTO dto) {
        return ResponseEntity.ok(usuarioService.banirProfessor(id, dto));
    }

    @PostMapping("/{id}/professores/desbanir")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<UsuarioDTO> desbanirProfessor(
            @PathVariable Long id,
            @Valid @RequestBody BanirProfessorDTO dto) {
        return ResponseEntity.ok(usuarioService.desbanirProfessor(id, dto));
    }

    @GetMapping("/{id}/professores/banidos")
    @PreAuthorize("@userAccess.canAccess(#id, authentication)")
    public ResponseEntity<Set<String>> listarProfessoresBanidos(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.listarProfessoresBanidos(id));
    }
}

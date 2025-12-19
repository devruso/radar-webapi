package com.jangada.RADAR.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.UsuarioMapper;
import com.jangada.RADAR.models.dtos.AtualizarDisciplinasDTO;
import com.jangada.RADAR.models.dtos.AtualizarTurnosDTO;
import com.jangada.RADAR.models.dtos.BanirProfessorDTO;
import com.jangada.RADAR.models.dtos.LoginDTO;
import com.jangada.RADAR.models.dtos.RegisterDTO;
import com.jangada.RADAR.models.dtos.UsuarioCadastroDTO;
import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.dtos.UsuarioTesteDTO;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.models.entities.Curso;
import com.jangada.RADAR.repositories.TurmaRepository;
import com.jangada.RADAR.repositories.CursoRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários e preferências")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             CursoRepository cursoRepository,
                             TurmaRepository turmaRepository,
                             UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listAll() {
        List<UsuarioDTO> dtos = usuarioRepository.findAll().stream().map(UsuarioMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getOne(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toDto(u)))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioDTO dto) {
        Usuario u = UsuarioMapper.toEntity(dto);

        // Vincula curso, se informado
        if (dto.getCursoId() != null) {
            Curso curso = cursoRepository.findById(dto.getCursoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + dto.getCursoId() + " não encontrado"));
            u.setCurso(curso);
        }

        // Vincula turmas selecionadas, se informadas
        if (dto.getTurmasSelecionadasIds() != null && !dto.getTurmasSelecionadasIds().isEmpty()) {
            for (Long turmaId : dto.getTurmasSelecionadasIds()) {
                Turma t = turmaRepository.findById(turmaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Turma com ID " + turmaId + " não encontrada"));
                u.getTurmasSelecionadas().add(t);
            }
        }

        Usuario saved = usuarioRepository.save(u);
        return ResponseEntity.ok(UsuarioMapper.toDto(saved));
    }

    @PostMapping("/teste")
    @Operation(summary = "Cria um usuário teste (sem cadastro)", description = "Permite uso da aplicação sem login")
    @ApiResponse(responseCode = "200", description = "Usuário teste criado com sucesso")
    public ResponseEntity<UsuarioDTO> criarTeste(@Valid @RequestBody UsuarioTesteDTO dto) {
        return ResponseEntity.ok(usuarioService.criarUsuarioTeste(dto));
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra novo usuário", description = "Cria conta com email e senha")
    @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso")
    public ResponseEntity<UsuarioDTO> cadastrar(@Valid @RequestBody UsuarioCadastroDTO dto) {
        return ResponseEntity.ok(usuarioService.cadastrarUsuario(dto));
    }

    @PostMapping("/{id}/disciplinas")
    @Operation(summary = "Atualiza disciplinas concluídas", description = "Define quais disciplinas o usuário já cursou")
    public ResponseEntity<UsuarioDTO> atualizarDisciplinas(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarDisciplinasDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarDisciplinas(id, dto));
    }

    @PostMapping("/{id}/turnos")
    @Operation(summary = "Atualiza turnos disponíveis", description = "Define quais turnos o usuário pode cursar")
    public ResponseEntity<UsuarioDTO> atualizarTurnos(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTurnosDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarTurnos(id, dto));
    }

    @PostMapping("/{id}/professores/banir")
    @Operation(summary = "Bane um professor", description = "Adiciona professor à lista de banidos")
    public ResponseEntity<UsuarioDTO> banirProfessor(
            @PathVariable Long id,
            @Valid @RequestBody BanirProfessorDTO dto) {
        return ResponseEntity.ok(usuarioService.banirProfessor(id, dto));
    }

    @PostMapping("/{id}/professores/desbanir")
    @Operation(summary = "Desbane um professor", description = "Remove professor da lista de banidos")
    public ResponseEntity<UsuarioDTO> desbanirProfessor(
            @PathVariable Long id,
            @Valid @RequestBody BanirProfessorDTO dto) {
        return ResponseEntity.ok(usuarioService.desbanirProfessor(id, dto));
    }

    @GetMapping("/{id}/professores/banidos")
    @Operation(summary = "Lista professores banidos", description = "Retorna todos os professores banidos pelo usuário")
    public ResponseEntity<java.util.Set<String>> listarProfessoresBanidos(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.listarProfessoresBanidos(id));
    }

    @PostMapping("/login")
    @Operation(summary = "Faz login do usuário", description = "Autentica usuário via email e senha")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public ResponseEntity<UsuarioDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }

    @PostMapping("/register")
    @Operation(summary = "Registra novo usuário (simples)", description = "Cria conta apenas com nome, email e senha")
    @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email já existe")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody RegisterDTO dto) {
        return ResponseEntity.ok(usuarioService.register(dto));
    }

}

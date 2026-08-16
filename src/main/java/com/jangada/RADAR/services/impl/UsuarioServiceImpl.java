package com.jangada.RADAR.services.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jangada.RADAR.exceptions.ResourceNotFoundException;
import com.jangada.RADAR.mappers.UsuarioMapper;
import com.jangada.RADAR.models.dtos.AtualizarDisciplinasDTO;
import com.jangada.RADAR.models.dtos.AtualizarPerfilDTO;
import com.jangada.RADAR.models.dtos.AtualizarTurnosDTO;
import com.jangada.RADAR.models.dtos.BanirProfessorDTO;
import com.jangada.RADAR.models.dtos.LoginDTO;
import com.jangada.RADAR.models.dtos.UsuarioCadastroDTO;
import com.jangada.RADAR.models.dtos.UsuarioDTO;
import com.jangada.RADAR.models.dtos.UsuarioTesteDTO;
import com.jangada.RADAR.models.entities.Curso;
import com.jangada.RADAR.models.entities.Usuario;
import com.jangada.RADAR.repositories.CursoRepository;
import com.jangada.RADAR.repositories.UsuarioRepository;
import com.jangada.RADAR.services.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO criarUsuarioTeste(UsuarioTesteDTO dto) {
        Curso curso = findCourse(dto.getCursoId());
        Usuario usuario = baseUser()
                .nome("Usuário Teste")
                .email("teste@radar.local")
                .anoIngresso(dto.getAnoIngresso())
                .mesIngresso(dto.getMesIngresso())
                .perfilInicial(dto.getPerfilInicial())
                .periodosRegularesCursados(dto.getPeriodosRegularesCursados())
                .periodoAtual(calcularSemestreAcademico(
                    dto.getPerfilInicial(), dto.getPeriodosRegularesCursados()
                ))
                .coeficienteRendimento(dto.getCoeficienteRendimento())
                .statusFormando(dto.getStatusFormando())
                .isTeste(true)
                .curso(curso)
                .build();
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO cadastrarUsuario(UsuarioCadastroDTO dto) {
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new IllegalArgumentException("Senhas não coincidem");
        }
        String email = normalizeEmail(dto.getEmail());
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        Usuario usuario = baseUser()
                .nome(dto.getNome().trim())
                .email(email)
                .senha(passwordEncoder.encode(dto.getSenha()))
                .anoIngresso(dto.getAnoIngresso())
                .mesIngresso(dto.getMesIngresso())
                .perfilInicial(dto.getPerfilInicial())
                .periodosRegularesCursados(dto.getPeriodosRegularesCursados())
                .periodoAtual(calcularSemestreAcademico(
                    dto.getPerfilInicial(), dto.getPeriodosRegularesCursados()
                ))
                .coeficienteRendimento(dto.getCoeficienteRendimento())
                .statusFormando(dto.getStatusFormando())
                .isTeste(false)
                .curso(findCourse(dto.getCursoId()))
                .build();
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO atualizarDisciplinas(Long usuarioId, AtualizarDisciplinasDTO dto) {
        Usuario usuario = findUser(usuarioId);
        usuario.setDisciplinasFeitas(new HashSet<>(dto.getDisciplinasFeitas()));
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO atualizarTurnos(Long usuarioId, AtualizarTurnosDTO dto) {
        Usuario usuario = findUser(usuarioId);
        usuario.setTurnosLivres(dto.getTurnosLivres());
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO atualizarPerfil(Long usuarioId, AtualizarPerfilDTO dto) {
        Usuario usuario = findUser(usuarioId);
        if (Boolean.TRUE.equals(usuario.getIsTeste())) {
            throw new IllegalArgumentException("Contas de teste não possuem perfil persistente editável");
        }
        String email = normalizeEmail(dto.email());
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(other -> !other.getId().equals(usuarioId))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
                });
        if (dto.novaSenha() != null && !dto.novaSenha().isBlank()) {
            if (dto.senhaAtual() == null || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
                throw new IllegalArgumentException("Senha atual inválida");
            }
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(email);
        usuario.setPerfilInicial(dto.perfilInicial());
        usuario.setPeriodosRegularesCursados(dto.periodosRegularesCursados());
        usuario.setPeriodoAtual(calcularSemestreAcademico(
            dto.perfilInicial(), dto.periodosRegularesCursados()
        ));
        usuario.setCoeficienteRendimento(dto.coeficienteRendimento());
        usuario.setStatusFormando(dto.statusFormando());
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO banirProfessor(Long usuarioId, BanirProfessorDTO dto) {
        Usuario usuario = findUser(usuarioId);
        usuario.getProfessoresExcluidos().add(dto.getProfessorNome());
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO desbanirProfessor(Long usuarioId, BanirProfessorDTO dto) {
        Usuario usuario = findUser(usuarioId);
        usuario.getProfessoresExcluidos().remove(dto.getProfessorNome());
        return UsuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public Set<String> listarProfessoresBanidos(Long usuarioId) {
        return Set.copyOf(findUser(usuarioId).getProfessoresExcluidos());
    }

    @Override
    public UsuarioDTO login(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizeEmail(dto.getEmail()))
                .filter(candidate -> candidate.getSenha() != null)
                .orElseThrow(() -> unauthorized());
        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw unauthorized();
        }
        return UsuarioMapper.toDto(usuario);
    }

    private Usuario findUser(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Curso findCourse(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    private Usuario.UsuarioBuilder baseUser() {
        return Usuario.builder()
                .disciplinasFeitas(new HashSet<>())
                .professoresExcluidos(new HashSet<>())
                .turmasSelecionadas(new HashSet<>());
    }

    private static int calcularSemestreAcademico(
        Integer perfilInicial,
        Integer periodosRegularesCursados
    ) {
        return perfilInicial + periodosRegularesCursados;
    }
}

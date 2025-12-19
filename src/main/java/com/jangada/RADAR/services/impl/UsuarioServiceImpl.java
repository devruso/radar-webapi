package com.jangada.RADAR.services.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                             CursoRepository cursoRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO criarUsuarioTeste(UsuarioTesteDTO dto) {
        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + dto.getCursoId() + " não encontrado"));
        
        Usuario usuario = Usuario.builder()
                .nome("Usuário Teste")
                .email("teste@radar.local")
                .anoIngresso(dto.getAnoIngresso())
                .mesIngresso(1) // Default janeiro
                .isTeste(true)
                .curso(curso)
                .disciplinasFeitas(new HashSet<>())
                .professoresExcluidos(new HashSet<>())
                .turmasSelecionadas(new HashSet<>())
                .build();
        
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public UsuarioDTO cadastrarUsuario(UsuarioCadastroDTO dto) {
        // Valida se senhas coincidem
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new IllegalArgumentException("Senhas não coincidem");
        }
        
        // Verifica se email já existe
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        
        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso com ID " + dto.getCursoId() + " não encontrado"));
        
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .anoIngresso(dto.getAnoIngresso())
                .mesIngresso(dto.getMesIngresso())
                .isTeste(false)
                .curso(curso)
                .disciplinasFeitas(new HashSet<>())
                .professoresExcluidos(new HashSet<>())
                .turmasSelecionadas(new HashSet<>())
                .build();
        
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public UsuarioDTO atualizarDisciplinas(Long usuarioId, AtualizarDisciplinasDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
        
        usuario.setDisciplinasFeitas(dto.getDisciplinasFeitas());
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public UsuarioDTO atualizarTurnos(Long usuarioId, AtualizarTurnosDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
        
        usuario.setTurnosLivres(dto.getTurnosLivres());
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public UsuarioDTO banirProfessor(Long usuarioId, BanirProfessorDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
        
        usuario.getProfessoresExcluidos().add(dto.getProfessorNome());
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public UsuarioDTO desbanirProfessor(Long usuarioId, BanirProfessorDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
        
        usuario.getProfessoresExcluidos().remove(dto.getProfessorNome());
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

    @Override
    public Set<String> listarProfessoresBanidos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
        
        return usuario.getProfessoresExcluidos();
    }

    @Override
    public UsuarioDTO login(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Credenciais inválidas"));
        
        // Verifica senha
        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new ResourceNotFoundException("Credenciais inválidas");
        }
        
        return UsuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDTO register(RegisterDTO dto) {
        // Verifica se email já existe
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email já cadastrado");
        }
        
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .matricula(dto.getMatricula())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .isTeste(false)
                .disciplinasFeitas(new HashSet<>())
                .professoresExcluidos(new HashSet<>())
                .turmasSelecionadas(new HashSet<>())
                .build();
        
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioMapper.toDto(saved);
    }

}

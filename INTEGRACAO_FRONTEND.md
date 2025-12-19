# 🔌 Guia de Integração Frontend - Next.js + TypeScript + Tailwind + shadcn

Documento prático para integrar o **RADAR API** com seu frontend em **Next.js**.

---

## 📋 Índice

1. [Setup do Projeto](#setup-do-projeto)
2. [Tipos TypeScript](#tipos-typescript)
3. [Client HTTP (Axios/Fetch)](#client-http)
4. [React Hooks Personalizados](#react-hooks)
5. [Componentes shadcn](#componentes-shadcn)
6. [Exemplos Práticos](#exemplos-práticos)
7. [Cache com React Query](#cache-com-react-query)
8. [Tratamento de Erros](#tratamento-de-erros)
9. [Autenticação](#autenticação)
10. [Deploy](#deploy)

---

## 🚀 Setup do Projeto

### Criar Projeto Next.js
```bash
npx create-next-app@latest radar-frontend --typescript --tailwind --shadcn-ui

# Ou se já existe:
npm install axios react-query dotenv
npm install -D @types/node @types/react
```

### Estrutura de Pastas Recomendada
```
src/
├── app/                    # App router Next.js
│   ├── layout.tsx
│   ├── page.tsx
│   └── recomendacoes/
│       └── page.tsx
├── lib/
│   ├── api/
│   │   ├── client.ts       # Cliente HTTP
│   │   ├── types.ts        # Tipos TypeScript
│   │   └── endpoints.ts    # URLs base
│   ├── hooks/
│   │   ├── useRecomendacoes.ts
│   │   ├── useAvaliacao.ts
│   │   └── useProfessores.ts
│   └── utils.ts
├── components/
│   ├── RecomendacaoCard.tsx
│   ├── AvaliacaoProfessor.tsx
│   ├── LoadingSpinner.tsx
│   └── ui/                 # Componentes shadcn
├── .env.local
└── package.json
```

### Arquivo .env.local
```env
# Backend API
NEXT_PUBLIC_API_BASE_URL=http://localhost:9090
NEXT_PUBLIC_API_TIMEOUT=10000

# Debug
DEBUG=false
```

---

## 📝 Tipos TypeScript

### `lib/api/types.ts`

```typescript
// ==================== ENTITIES ====================

export interface Usuario {
  id: number;
  nome: string;
  matricula: string;
  email: string;
  limiteMatricula: number;
  tempoEstudo: number;
  tempoTransporte: number;
  anoIngresso: number;
  periodoAtual: number;
  turnosLivres: string[];
  professoresExcluidos: string[];
  disciplinasFeitas: number[];
  turmasSelecionadasIds: number[];
  cursoId: number;
}

export interface Curso {
  id: number;
  nome: string;
  codigo: string;
  descricao: string;
  estruturaId: number;
  guiaId: number;
}

export interface ComponenteCurricular {
  id: number;
  nome: string;
  codigo: string;
  creditos: number;
  carga_horaria: number;
  nivel: number; // 1-10
  descricao: string;
}

export interface Turma {
  id: number;
  componenteId: number;
  professorNome: string;
  periodo: number;
  ano: number;
  vagas: number;
  horariosIds: number[];
}

export interface Horario {
  id: number;
  diaSemana: string;
  horaInicio: string;
  horaFim: string;
}

export interface AvaliacaoProfessor {
  id: number;
  usuarioId: number;
  professorNome: string;
  componenteId: number;
  nota: number; // 1-5
  comentario?: string;
  dataAvaliacao: string;
}

export interface PreRequisito {
  id: number;
  componenteId: number;
  componentePreRequisitoId: number;
  tipo: "PREREQUISITO" | "COREQUISITO" | "POSREQUISITO";
}

// ==================== DTOs ====================

export interface RecomendacaoTurmaDTO {
  turma: Turma;
  dificuldade: "FACIL" | "INTERMEDIO" | "DIFICIL";
  scoreProfessor: number; // 1-5
  motivo: string;
  posicao: number; // 1-8
}

// ==================== API RESPONSES ====================

export interface ApiResponse<T> {
  data: T;
  timestamp: string;
  status: number;
  error?: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ==================== REQUEST PAYLOADS ====================

export interface AvaliarProfessorPayload {
  usuarioId: number;
  professorNome: string;
  componenteId: number;
  nota: number; // 1-5
  comentario?: string;
}

export interface CriarPreRequisitoPayload {
  componenteId: number;
  componentePreRequisitoId: number;
  tipo: "PREREQUISITO" | "COREQUISITO" | "POSREQUISITO";
}
```

---

## 🔌 Client HTTP

### `lib/api/client.ts`

```typescript
import axios, {
  AxiosInstance,
  AxiosError,
  InternalAxiosRequestConfig,
} from "axios";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:9090";
const API_TIMEOUT = Number(process.env.NEXT_PUBLIC_API_TIMEOUT || 10000);

// Criar instância Axios
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor de Request
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Adicionar token se existir (para autenticação futura)
    const token = localStorage.getItem("auth_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (process.env.DEBUG === "true") {
      console.log(`📡 [${config.method?.toUpperCase()}] ${config.url}`);
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor de Response
apiClient.interceptors.response.use(
  (response) => {
    if (process.env.DEBUG === "true") {
      console.log(`✅ [${response.status}] ${response.config.url}`);
    }
    return response;
  },
  (error: AxiosError) => {
    if (process.env.DEBUG === "true") {
      console.error(
        `❌ [${error.response?.status}] ${error.config?.url}`,
        error.response?.data
      );
    }

    // Tratamento específico de erros
    if (error.response?.status === 401) {
      // Token expirado
      localStorage.removeItem("auth_token");
      window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

### `lib/api/endpoints.ts`

```typescript
// URL base da API
export const API_ENDPOINTS = {
  // Recomendações
  RECOMENDACOES: {
    GERAR: (usuarioId: number) => `/api/recomendacoes/gerar/${usuarioId}`,
    AVALIAR_PROFESSOR: "/api/recomendacoes/avaliar-professor",
    AVALIACOES_PROFESSOR: (nome: string) => `/api/recomendacoes/professor/${nome}/avaliacoes`,
    SCORE_PROFESSOR: (nome: string, componenteId?: number) =>
      `/api/recomendacoes/professor/${nome}/score${componenteId ? `?componenteId=${componenteId}` : ""}`,
  },

  // Cursos
  CURSOS: {
    LIST: "/api/cursos",
    DETAIL: (id: number) => `/api/cursos/${id}`,
  },

  // Turmas
  TURMAS: {
    LIST: "/api/turmas",
    DETAIL: (id: number) => `/api/turmas/${id}`,
    BY_CURSO: (cursoId: number) => `/api/turmas/curso/${cursoId}`,
  },

  // Usuários
  USUARIOS: {
    LIST: "/api/usuarios",
    DETAIL: (id: number) => `/api/usuarios/${id}`,
  },

  // Avaliações de Professores
  AVALIACOES: {
    LIST: "/api/avaliacoes-professor",
    DETAIL: (id: number) => `/api/avaliacoes-professor/${id}`,
    BY_USUARIO: (usuarioId: number) => `/api/avaliacoes-professor/usuario/${usuarioId}`,
    BY_PROFESSOR: (nome: string) => `/api/avaliacoes-professor/professor/${nome}`,
  },

  // Pré-requisitos
  PREREQUISITOS: {
    LIST: "/api/prerequisitos",
    DETAIL: (id: number) => `/api/prerequisitos/${id}`,
    BY_COMPONENTE: (componenteId: number) => `/api/prerequisitos/componente/${componenteId}`,
    BY_COMPONENTE_TIPO: (componenteId: number, tipo: string) =>
      `/api/prerequisitos/componente/${componenteId}/tipo/${tipo}`,
  },
};
```

---

## ⚛️ React Hooks

### `lib/hooks/useRecomendacoes.ts`

```typescript
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { RecomendacaoTurmaDTO, AvaliarProfessorPayload } from "@/lib/api/types";

// Hook para buscar recomendações
export function useRecomendacoes(usuarioId: number, metodo: string = "burrinho") {
  return useQuery({
    queryKey: ["recomendacoes", usuarioId, metodo],
    queryFn: async () => {
      const response = await apiClient.post<RecomendacaoTurmaDTO[]>(
        API_ENDPOINTS.RECOMENDACOES.GERAR(usuarioId),
        {},
        { params: { metodo } }
      );
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutos
    retry: 3,
  });
}

// Hook para avaliar professor
export function useAvaliarProfessor() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: AvaliarProfessorPayload) => {
      const response = await apiClient.post(
        API_ENDPOINTS.RECOMENDACOES.AVALIAR_PROFESSOR,
        payload
      );
      return response.data;
    },
    onSuccess: (data, variables) => {
      // Invalidar cache de avaliações
      queryClient.invalidateQueries({
        queryKey: ["avaliacoes", variables.usuarioId],
      });
      queryClient.invalidateQueries({
        queryKey: ["score-professor", variables.professorNome],
      });
    },
    onError: (error) => {
      console.error("Erro ao avaliar professor:", error);
    },
  });
}

// Hook para buscar score do professor
export function useScoreProfessor(nome: string, componenteId?: number) {
  return useQuery({
    queryKey: ["score-professor", nome, componenteId],
    queryFn: async () => {
      const response = await apiClient.get<number>(
        API_ENDPOINTS.RECOMENDACOES.SCORE_PROFESSOR(nome, componenteId)
      );
      return response.data;
    },
    enabled: !!nome,
    staleTime: 10 * 60 * 1000, // 10 minutos
  });
}

// Hook para buscar avaliações de um professor
export function useAvaliacoesProfessor(nome: string) {
  return useQuery({
    queryKey: ["avaliacoes-professor", nome],
    queryFn: async () => {
      const response = await apiClient.get(
        API_ENDPOINTS.RECOMENDACOES.AVALIACOES_PROFESSOR(nome)
      );
      return response.data;
    },
    enabled: !!nome,
    staleTime: 10 * 60 * 1000,
  });
}
```

### `lib/hooks/useProfessores.ts`

```typescript
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

export function useAvaliacoesProfessor(usuarioId: number) {
  return useQuery({
    queryKey: ["avaliacoes", usuarioId],
    queryFn: async () => {
      const response = await apiClient.get(
        API_ENDPOINTS.AVALIACOES.BY_USUARIO(usuarioId)
      );
      return response.data;
    },
    enabled: !!usuarioId,
    staleTime: 5 * 60 * 1000,
  });
}

export function useAvaliacaoProfessor(id: number) {
  return useQuery({
    queryKey: ["avaliacao", id],
    queryFn: async () => {
      const response = await apiClient.get(
        API_ENDPOINTS.AVALIACOES.DETAIL(id)
      );
      return response.data;
    },
    enabled: !!id,
  });
}
```

---

## 🎨 Componentes shadcn

### Instalar Componentes Necessários
```bash
npx shadcn-ui@latest add card
npx shadcn-ui@latest add button
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add skeleton
npx shadcn-ui@latest add alert
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add tabs
```

### `components/RecomendacaoCard.tsx`

```typescript
import { RecomendacaoTurmaDTO } from "@/lib/api/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Star } from "lucide-react";

interface Props {
  recomendacao: RecomendacaoTurmaDTO;
  onSelect?: (recomendacao: RecomendacaoTurmaDTO) => void;
}

export function RecomendacaoCard({ recomendacao, onSelect }: Props) {
  const { turma, dificuldade, scoreProfessor, motivo, posicao } = recomendacao;

  // Cores por dificuldade
  const dificuldadeColor: Record<string, string> = {
    FACIL: "bg-green-100 text-green-800",
    INTERMEDIO: "bg-yellow-100 text-yellow-800",
    DIFICIL: "bg-red-100 text-red-800",
  };

  // Estrelas do professor
  const renderStars = (score: number) => {
    return Array.from({ length: 5 }).map((_, i) => (
      <Star
        key={i}
        size={16}
        className={i < Math.round(score) ? "fill-yellow-400 text-yellow-400" : "text-gray-300"}
      />
    ));
  };

  return (
    <Card className="hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-2xl font-bold text-primary">#{posicao}</span>
              <Badge className={dificuldadeColor[dificuldade]}>
                {dificuldade}
              </Badge>
            </div>
            <CardTitle className="text-lg">
              {/* turma.componenteNome - você pode ajustar conforme sua estrutura */}
            </CardTitle>
          </div>
          <div className="text-right">
            <div className="flex gap-1">{renderStars(scoreProfessor)}</div>
            <span className="text-sm text-gray-600">{scoreProfessor.toFixed(1)}/5</span>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-gray-600 mb-4">
          <span className="font-semibold">Professor:</span> {turma.professorNome}
        </p>

        <p className="text-sm mb-4 p-3 bg-blue-50 rounded">
          <span className="font-semibold text-blue-900">Motivo:</span>
          <br />
          {motivo}
        </p>

        <div className="flex gap-2">
          <Button
            onClick={() => onSelect?.(recomendacao)}
            className="flex-1"
            variant="default"
          >
            Selecionar
          </Button>
          <Button variant="outline" className="flex-1">
            Mais Info
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
```

### `components/AvaliacaoProfessor.tsx`

```typescript
"use client";

import { useState } from "react";
import { useAvaliarProfessor } from "@/lib/hooks/useRecomendacoes";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Star } from "lucide-react";

interface Props {
  usuarioId: number;
  professorNome: string;
  componenteId: number;
}

export function AvaliacaoProfessor({ usuarioId, professorNome, componenteId }: Props) {
  const [nota, setNota] = useState(0);
  const [comentario, setComentario] = useState("");
  const { mutate, isPending, isSuccess } = useAvaliarProfessor();

  const handleSubmit = () => {
    mutate({
      usuarioId,
      professorNome,
      componenteId,
      nota,
      comentario,
    });
  };

  if (isSuccess) {
    return (
      <Card className="bg-green-50 border-green-200">
        <CardContent className="pt-6">
          <p className="text-green-800">✓ Avaliação registrada com sucesso!</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Avaliar Professor</CardTitle>
        <p className="text-sm text-gray-600 mt-2">{professorNome}</p>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Rating Stars */}
        <div>
          <label className="block text-sm font-semibold mb-2">Sua Avaliação</label>
          <div className="flex gap-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <button
                key={i}
                onClick={() => setNota(i + 1)}
                className="transition-transform hover:scale-110"
              >
                <Star
                  size={28}
                  className={
                    i < nota
                      ? "fill-yellow-400 text-yellow-400"
                      : "text-gray-300"
                  }
                />
              </button>
            ))}
          </div>
          <p className="text-sm text-gray-600 mt-2">{nota} de 5 estrelas</p>
        </div>

        {/* Comentário */}
        <div>
          <label className="block text-sm font-semibold mb-2">
            Comentário (opcional)
          </label>
          <textarea
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            placeholder="Seu feedback..."
            className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
          />
        </div>

        {/* Botão Submit */}
        <Button
          onClick={handleSubmit}
          disabled={nota === 0 || isPending}
          className="w-full"
        >
          {isPending ? "Enviando..." : "Enviar Avaliação"}
        </Button>
      </CardContent>
    </Card>
  );
}
```

---

## 📚 Exemplos Práticos

### Página de Recomendações

`app/recomendacoes/page.tsx`:

```typescript
"use client";

import { useState } from "react";
import { useRecomendacoes } from "@/lib/hooks/useRecomendacoes";
import { RecomendacaoCard } from "@/components/RecomendacaoCard";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function RecomendacoesPage() {
  const usuarioId = 1; // Vem do contexto/sessão em produção
  const { data: recomendacoes, isLoading, error } = useRecomendacoes(usuarioId);

  if (isLoading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 3 }).map((_, i) => (
          <Skeleton key={i} className="h-40" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertDescription>
          Erro ao carregar recomendações. Tente novamente.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Suas Recomendações</h1>

      <div className="grid gap-4 md:grid-cols-2">
        {recomendacoes?.map((rec, idx) => (
          <RecomendacaoCard
            key={idx}
            recomendacao={rec}
            onSelect={(recomendacao) => {
              console.log("Selecionado:", recomendacao);
              // Implementar lógica de seleção
            }}
          />
        ))}
      </div>

      <Button size="lg" className="w-full">
        Confirmar Seleção
      </Button>
    </div>
  );
}
```

---

## 🚀 Cache com React Query

### Setup no Layout Principal

`app/layout.tsx`:

```typescript
"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { ReactNode, useState } from "react";

export default function RootLayout({ children }: { children: ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000, // 1 minuto
            retry: 1,
          },
        },
      })
  );

  return (
    <html>
      <body>
        <QueryClientProvider client={queryClient}>
          {children}
          <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
      </body>
    </html>
  );
}
```

---

## ⚠️ Tratamento de Erros

### `lib/utils/errorHandler.ts`

```typescript
import { AxiosError } from "axios";

export interface ErrorDetails {
  status: number;
  message: string;
  field?: string;
}

export function handleApiError(error: unknown): ErrorDetails {
  if (error instanceof AxiosError) {
    if (error.response?.status === 404) {
      return {
        status: 404,
        message: "Recurso não encontrado",
      };
    }

    if (error.response?.status === 400) {
      return {
        status: 400,
        message: error.response.data?.message || "Dados inválidos",
      };
    }

    if (error.response?.status === 500) {
      return {
        status: 500,
        message: "Erro no servidor. Tente novamente mais tarde.",
      };
    }

    return {
      status: error.response?.status || 0,
      message: error.message,
    };
  }

  return {
    status: 0,
    message: "Erro desconhecido",
  };
}

export function isNetworkError(error: unknown): boolean {
  if (error instanceof AxiosError) {
    return error.code === "ECONNABORTED" || error.code === "ERR_NETWORK";
  }
  return false;
}
```

---

## 🔐 Autenticação (Futuro)

### Setup para JWT (quando implementado)

`lib/hooks/useAuth.ts`:

```typescript
import { useQuery, useMutation } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";

export function useAuth() {
  const login = useMutation({
    mutationFn: async (credentials: { email: string; password: string }) => {
      const response = await apiClient.post("/api/auth/login", credentials);
      const { token } = response.data;

      // Salvar token
      localStorage.setItem("auth_token", token);

      return response.data;
    },
  });

  const logout = () => {
    localStorage.removeItem("auth_token");
    window.location.href = "/login";
  };

  const getCurrentUser = useQuery({
    queryKey: ["current-user"],
    queryFn: async () => {
      const response = await apiClient.get("/api/auth/me");
      return response.data;
    },
  });

  return { login, logout, getCurrentUser };
}
```

---

## 🌐 Deploy

### Variáveis de Ambiente em Produção

`.env.production`:

```env
NEXT_PUBLIC_API_BASE_URL=https://api.seu-dominio.com
NEXT_PUBLIC_API_TIMEOUT=15000
```

### Build & Deploy

```bash
# Build
npm run build

# Vercel (recomendado)
npm install -g vercel
vercel

# Docker
docker build -t radar-frontend .
docker run -p 3000:3000 radar-frontend
```

### Dockerfile

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY package*.json ./
RUN npm ci --production

EXPOSE 3000
CMD ["npm", "start"]
```

---

## 📋 Checklist de Integração

- [ ] Tipos TypeScript definidos (`lib/api/types.ts`)
- [ ] Cliente HTTP configurado (`lib/api/client.ts`)
- [ ] Endpoints mapeados (`lib/api/endpoints.ts`)
- [ ] Hooks React Query criados (`lib/hooks/`)
- [ ] Componentes shadcn instalados e customizados
- [ ] Página de recomendações funcionando
- [ ] Cache com React Query implementado
- [ ] Tratamento de erros global
- [ ] Variáveis de ambiente configuradas
- [ ] Deploy em staging testado

---

## 🆘 Troubleshooting

### "CORS error"
```
Solução: Verificar se Backend tem CORS habilitado
Backend: SecurityConfig permitir origem do frontend
```

### "API não conecta"
```
Solução: Verificar se Backend está rodando na porta 9090
Comando: netstat -ano | findstr :9090
```

### "Dados desatualizados"
```
Solução: Ajustar staleTime dos hooks
Aumentar: staleTime: 10 * 60 * 1000 (10 minutos)
```

---

## 📞 Suporte

Para dúvidas sobre integração:
- 📧 Email: [contato]
- 💬 Discord: [link]
- 🐛 Issues: GitHub Issues

---

<div align="center">

**Desenvolvido para facilitar a integração Frontend ↔ Backend**

[⬆ Voltar ao topo](#-guia-de-integração-frontend---nextjs--typescript--tailwind--shadcn)

</div>

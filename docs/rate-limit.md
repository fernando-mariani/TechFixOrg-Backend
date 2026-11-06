# Limites de requisições

Todas as rotas passam por um limite de 600 requisições por minuto por IP, antes da autenticação JWT e da consulta ao usuário. O login também permite no máximo 5 tentativas por combinação de e-mail e IP em 15 minutos, antes da busca no banco e da comparação BCrypt. Contam tentativas bem-sucedidas e malsucedidas, inclusive para e-mails inexistentes. Maiúsculas e espaços nas extremidades não criam novos contadores.

Ao exceder um limite, a API responde HTTP 429, JSON com `status` e `mensagem`, e cabeçalho `Retry-After` em segundos. O desbloqueio é automático; requisições bloqueadas não prolongam o período. As janelas são fixas, iniciadas na primeira requisição, e podem permitir rajadas nas suas fronteiras.

Configuração por variáveis de ambiente:

| Variável | Padrão |
| --- | --- |
| RATE_LIMIT_GENERAL | 600 |
| RATE_LIMIT_GENERAL_WINDOW | PT1M |
| RATE_LIMIT_LOGIN | 5 |
| RATE_LIMIT_LOGIN_WINDOW | PT15M |
| RATE_LIMIT_MAX_ENTRIES | 50000 |

As durações usam ISO-8601. Cada contador mantém no máximo 50000 chaves, com remoção de entradas expiradas na próxima requisição. Quando cheio, recusa novas chaves até liberar espaço, sem descartar bloqueios ativos. Os limites são atômicos entre threads, mas locais à instância e reiniciam ao reiniciar a aplicação. Para múltiplas réplicas, usar armazenamento compartilhado (por exemplo Redis) ou aplicar os limites no gateway.

O IP vem de `getRemoteAddr()`. O filtro não interpreta `X-Forwarded-For`. Atrás de proxy, configurar a infraestrutura para aceitar encaminhamento apenas de proxies confiáveis e impedir acesso direto à aplicação; sem isso, clientes podem compartilhar o limite do proxy. Não habilitar confiança irrestrita em cabeçalhos enviados pela internet.

Estes limites reduzem abuso na aplicação. Proteção contra DDoS volumétrico exige CDN/WAF/gateway, além de limites de conexões e tamanho de corpo no servidor ou proxy. Tentativas para o mesmo e-mail em IPs diferentes têm contadores independentes. Dispositivos que compartilham o mesmo IP público também compartilham o contador para o mesmo e-mail. Ajustar os valores conforme tráfego e monitoramento.

Referência para escolha inicial do limite de login: [OWASP WSTG](https://owasp.github.io/www-project-web-security-testing-guide/v41/4-Web_Application_Security_Testing/04-Authentication_Testing/03-Testing_for_Weak_Lock_Out_Mechanism). O limite geral é um ponto de partida operacional, sem padrão universal.


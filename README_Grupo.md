Grupo de Testes do Sistema BiblioTech

👥 Integrantes do Grupo

Luã Oliveira Souza

Maria Clara Mato Almeida

✅ Como Executar os Testes / Pré-requisitos

Java 17+

Maven 3.6+

Google Chrome instalado

IDE recomendada: Eclipse, IntelliJ IDEA, VS Code, etc.

📌 Passo a passo
1. Clonar o repositório
git clone git@github.com:Lua-Sdy/bibliotech_Trabalho_Final.git

2. Certificar-se que o sistema está em execução

Acesse no navegador:
🔗 http://localhost:8080

Credenciais padrão para login:

admin@bibliotech.com
 / admin123

3. Executar os testes automatizados

Os testes Selenium abrirão o navegador automaticamente.

As evidências (prints) são geradas para cenários de sucesso e falha e ficam disponíveis em:

evidencias/screenshots/

📊 Resultados Obtidos (Resumo)

Métrica	Resultado

Total de testes Selenium	15

Testes aprovados	11 (73,3%)

Testes reprovados	4 (26,7%)

Bugs identificados	4 críticos / alto impacto

🐞 Principais Bugs Encontrados

Código	Descrição

BUG-001	Login falha com credenciais válidas devido ao uso de == em vez de .equals() na validação de senha.

BUG-002	Sistema permite cadastro de livros com ISBN duplicado (violando RN-02).

BUG-003	Multa calculada incorretamente como R$ 3,00/dia (correto: R$ 2,00/dia).

BUG-004	Prazo de empréstimo definido como 7 dias (correto: 14 dias).

Todos os bugs foram confirmados via testes automatizados e possuem evidências visuais.

📄 Relatório Completo

🔗 Relatório PDF disponível no repositório


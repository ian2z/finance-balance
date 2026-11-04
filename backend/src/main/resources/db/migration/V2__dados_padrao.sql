-- Formas de pagamento padrao
INSERT INTO formas_pagamento (nome) VALUES
    ('Dinheiro'), ('Pix'), ('Cartão de débito'), ('Cartão de crédito'), ('Transferência bancária'), ('Boleto');

-- Categorias padrao do sistema (usuario_id NULL)
INSERT INTO categorias (nome, tipo) VALUES
    ('Alimentação', 'DESPESA'),
    ('Transporte', 'DESPESA'),
    ('Moradia', 'DESPESA'),
    ('Educação', 'DESPESA'),
    ('Saúde', 'DESPESA'),
    ('Lazer', 'DESPESA'),
    ('Outras despesas', 'DESPESA'),
    ('Salário', 'RECEITA'),
    ('Bolsa e auxílios', 'RECEITA'),
    ('Freelance', 'RECEITA'),
    ('Rendimentos', 'RECEITA'),
    ('Outras receitas', 'RECEITA');

INSERT INTO categorias (nome, tipo, categoria_pai_id)
SELECT s.nome, 'DESPESA', c.id
FROM (VALUES
    ('Mercado', 'Alimentação'), ('Restaurante', 'Alimentação'), ('Delivery', 'Alimentação'),
    ('Ônibus', 'Transporte'), ('Combustível', 'Transporte'), ('Aplicativos de transporte', 'Transporte'),
    ('Faculdade', 'Educação'), ('Cursos', 'Educação'), ('Material acadêmico', 'Educação')
) AS s(nome, pai)
JOIN categorias c ON c.nome = s.pai AND c.usuario_id IS NULL AND c.categoria_pai_id IS NULL;

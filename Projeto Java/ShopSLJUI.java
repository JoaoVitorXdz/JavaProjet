import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ShopSLJUI extends JFrame {
    private DefaultListModel<Produto> produtosModel;
    private DefaultListModel<ItemCarrinho> carrinhoModel;
    private JList<Produto> produtosList;
    private JList<ItemCarrinho> carrinhoList;
    private JLabel totalLabel;
    private double total = 0.0;

    public ShopSLJUI() {
        setTitle("SLJ Store");
        setSize(780, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Lista de produtos com imagens e estoque
        java.util.List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto("Camiseta", 59.90, "imagens/camiseta.png", 10));
        produtos.add(new Produto("Caneca", 29.90, "imagens/caneca.png", 10));
        produtos.add(new Produto("Boné", 39.90, "imagens/bone.png", 10));
        produtos.add(new Produto("Mochila", 89.90, "imagens/mochila.png", 10));
        produtos.add(new Produto("Adesivo", 9.90, "imagens/adesivo.png", 10));

        produtosModel = new DefaultListModel<>();
        for (Produto p : produtos) produtosModel.addElement(p);

        produtosList = new JList<>(produtosModel);
        produtosList.setCellRenderer(new ProdutoRenderer());
        produtosList.setBorder(BorderFactory.createTitledBorder("Produtos disponíveis"));
        JScrollPane scrollProdutos = new JScrollPane(produtosList);

        // Carrinho com quantidades
        carrinhoModel = new DefaultListModel<>();
        carrinhoList = new JList<>(carrinhoModel);
        carrinhoList.setCellRenderer(new ItemCarrinhoRenderer());
        carrinhoList.setBorder(BorderFactory.createTitledBorder("Carrinho"));
        JScrollPane scrollCarrinho = new JScrollPane(carrinhoList);

        // Botões
        JButton btnAdicionar = new JButton("Adicionar ao carrinho");
        JButton btnRemover = new JButton("Remover do carrinho");
        JButton btnFinalizar = new JButton("Finalizar pedido");

        totalLabel = new JLabel("Total: R$ 0.00");

        JPanel botoesPanel = new JPanel();
        botoesPanel.add(btnAdicionar);
        botoesPanel.add(btnRemover);
        botoesPanel.add(btnFinalizar);
        botoesPanel.add(totalLabel);

        JPanel listasPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listasPanel.add(scrollProdutos);
        listasPanel.add(scrollCarrinho);

        add(listasPanel, BorderLayout.CENTER);
        add(botoesPanel, BorderLayout.SOUTH);

        // Eventos
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnRemover.addActionListener(e -> removerProduto());
        btnFinalizar.addActionListener(e -> finalizarPedido());
    }

    private void adicionarProduto() {
        Produto p = produtosList.getSelectedValue();
        if (p != null) {
            if (p.getEstoque() <= 0) {
                JOptionPane.showMessageDialog(this, "Produto sem estoque!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean encontrado = false;
            for (int i = 0; i < carrinhoModel.size(); i++) {
                ItemCarrinho item = carrinhoModel.getElementAt(i);
                if (item.getProduto().equals(p)) {
                    item.aumentarQuantidade();
                    carrinhoList.repaint();
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                carrinhoModel.addElement(new ItemCarrinho(p));
            }

            p.retirarEstoque(1);
            produtosList.repaint();
            total += p.getPreco();
            atualizarTotal();
        }
    }

    private void removerProduto() {
        ItemCarrinho item = carrinhoList.getSelectedValue();
        if (item != null) {
            item.diminuirQuantidade();
            item.getProduto().adicionarEstoque(1);
            total -= item.getProduto().getPreco();

            if (item.getQuantidade() <= 0) {
                carrinhoModel.removeElement(item);
            }

            atualizarTotal();
            produtosList.repaint();
            carrinhoList.repaint();
        }
    }

    private void finalizarPedido() {
        if (carrinhoModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O carrinho está vazio!", "Aviso", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Pedido finalizado com sucesso!\nTotal: R$ " + String.format("%.2f", total),
                    "SLJ Store", JOptionPane.INFORMATION_MESSAGE);
            carrinhoModel.clear();
            total = 0;
            atualizarTotal();
        }
    }

    private void atualizarTotal() {
        totalLabel.setText("Total: R$ " + String.format("%.2f", total));
    }

    // Classe Produto com controle de estoque
    static class Produto {
        private String nome;
        private double preco;
        private String imagemPath;
        private int estoque;

        public Produto(String nome, double preco, String imagemPath, int estoque) {
            this.nome = nome;
            this.preco = preco;
            this.imagemPath = imagemPath;
            this.estoque = estoque;
        }

        public String getNome() { return nome; }
        public double getPreco() { return preco; }
        public String getImagemPath() { return imagemPath; }
        public int getEstoque() { return estoque; }

        public void retirarEstoque(int q) { estoque -= q; }
        public void adicionarEstoque(int q) { estoque += q; }

        @Override
        public String toString() {
            return nome + " - R$ " + String.format("%.2f", preco) + " (Estoque: " + estoque + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Produto)) return false;
            Produto p = (Produto) o;
            return nome.equals(p.nome);
        }
    }

    // Classe para itens no carrinho
    static class ItemCarrinho {
        private Produto produto;
        private int quantidade;

        public ItemCarrinho(Produto produto) {
            this.produto = produto;
            this.quantidade = 1;
        }

        public Produto getProduto() { return produto; }
        public int getQuantidade() { return quantidade; }
        public void aumentarQuantidade() { quantidade++; }
        public void diminuirQuantidade() { quantidade--; }

        @Override
        public String toString() {
            return produto.getNome() + " (x" + quantidade + ")";
        }
    }

    // Renderer dos produtos com imagem e estoque
    static class ProdutoRenderer extends JLabel implements ListCellRenderer<Produto> {
        public ProdutoRenderer() {
            setOpaque(true);
            setIconTextGap(10);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Produto> list, Produto produto, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(produto.getNome() + " - R$ " + String.format("%.2f", produto.getPreco()) +
                    " | Estoque: " + produto.getEstoque());
            ImageIcon icon = new ImageIcon(produto.getImagemPath());
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(img));

            if (isSelected) {
                setBackground(new Color(173, 216, 230));
            } else {
                setBackground(Color.WHITE);
            }

            return this;
        }
    }

    // Renderer dos itens do carrinho
    static class ItemCarrinhoRenderer extends JLabel implements ListCellRenderer<ItemCarrinho> {
        public ItemCarrinhoRenderer() {
            setOpaque(true);
            setIconTextGap(10);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ItemCarrinho> list, ItemCarrinho item, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Produto p = item.getProduto();
            setText(p.getNome() + " - R$ " + String.format("%.2f", p.getPreco()) + "  (x" + item.getQuantidade() + ")");
            ImageIcon icon = new ImageIcon(p.getImagemPath());
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(img));

            if (isSelected) {
                setBackground(new Color(173, 216, 230));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShopSLJUI().setVisible(true));
    }
}

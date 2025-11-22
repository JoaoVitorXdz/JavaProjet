
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

class Product {
    private int id;
    private String name;
    private double price;
    private int stock;

    public Product(int id, String name, double price, int stock){
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    public int getId(){ return id; }
    public String getName(){ return name; }
    public double getPrice(){ return price; }
    public int getStock(){ return stock; }
    public void setStock(int s){ this.stock = s; }
}

class CartItem {
    private Product product;
    private int quantity;
    public CartItem(Product p, int q){ this.product = p; this.quantity = q; }
    public Product getProduct(){ return product; }
    public int getQuantity(){ return quantity; }
    public void setQuantity(int q){ this.quantity = q; }
    public double getTotal(){ return quantity * product.getPrice(); }
}

class Cart {
    private java.util.List<CartItem> items = new ArrayList<>();
    public void add(Product p, int qty){
        for(CartItem it: items){
            if(it.getProduct().getId() == p.getId()){
                it.setQuantity(it.getQuantity() + qty);
                return;
            }
        }
        items.add(new CartItem(p, qty));
    }
    public void remove(Product p){
        items.removeIf(it -> it.getProduct().getId() == p.getId());
    }
    public java.util.List<CartItem> getItems(){ return items; }
    public double getTotal(){
        double s = 0;
        for(CartItem it: items) s += it.getTotal();
        return s;
    }
    public void clear(){ items.clear(); }
}

public class ShopSLJ extends JFrame {
    private java.util.List<Product> products = new ArrayList<>();
    private Cart cart = new Cart();
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private DecimalFormat df = new DecimalFormat("#0.00");

    public ShopSLJ(){
        setTitle("Loja Virtual - ShopSLJ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,500);
        setLocationRelativeTo(null);
        initProducts();
        initUI();
    }

    private void initProducts(){
        products.add(new Product(1, "Camisa Polo", 79.90, 10));
        products.add(new Product(2, "Calça Jeans", 129.90, 8));
        products.add(new Product(3, "Tênis Esportivo", 199.90, 5));
        products.add(new Product(4, "Boné", 39.90, 20));
        products.add(new Product(5, "Mochila", 149.90, 6));
    }

    private void initUI(){
        setLayout(new BorderLayout());

        // Top label
        JLabel header = new JLabel("Loja Virtual - Produtos", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // Tabela
        String[] cols = {"ID","Produto","Preço (R$)","Estoque","Ação"};
        tableModel = new DefaultTableModel(null, cols) {
            public boolean isCellEditable(int row, int column) {
                return column == 4; // apenas coluna Ação editável (botão)
            }
        };
        table = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // Populate table
        refreshTable();

        // Renderer o Preço e Estoque
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){
            public void setValue(Object value){
                setText(value == null ? "" : value.toString());
            }
        });

        // Botão Painel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(260, getHeight()));

        JButton viewCartBtn = new JButton("Ver Carrinho");
        viewCartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewCartBtn.addActionListener(e -> showCartDialog());

        JButton checkoutBtn = new JButton("Finalizar Compra");
        checkoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutBtn.addActionListener(e -> finalizePurchase());

        totalLabel = new JLabel("Total do Carrinho: R$ 0.00", SwingConstants.CENTER);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(viewCartBtn);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(checkoutBtn);
        rightPanel.add(Box.createVerticalGlue());

        add(rightPanel, BorderLayout.EAST);

        // Adiciona Botão Renderer e Editor
        table.getColumn("Ação").setCellRenderer(new ButtonRenderer());
        table.getColumn("Ação").setCellEditor(new ButtonEditor(new JCheckBox()));

    }

    private void refreshTable(){
        tableModel.setRowCount(0);
        for(Product p : products){
            tableModel.addRow(new Object[]{p.getId(), p.getName(), df.format(p.getPrice()), p.getStock(), "Adicionar"});
        }
    }

    private void showCartDialog(){
        JDialog dlg = new JDialog(this, "Carrinho", true);
        dlg.setSize(500,400);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout());

        String[] cols = {"Produto","Preço (R$)","Quantidade","Subtotal (R$)"};
        DefaultTableModel m = new DefaultTableModel(null, cols);
        JTable t = new JTable(m);
        for(CartItem it: cart.getItems()){
            m.addRow(new Object[]{
                it.getProduct().getName(),
                df.format(it.getProduct().getPrice()),
                it.getQuantity(),
                df.format(it.getTotal())
            });
        }

        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JLabel total = new JLabel("Total: R$ " + df.format(cart.getTotal()), SwingConstants.RIGHT);
        total.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        p.add(total, BorderLayout.SOUTH);

        dlg.add(p);
        dlg.setVisible(true);
    }

    private void finalizePurchase(){
        if(cart.getItems().isEmpty()){
            JOptionPane.showMessageDialog(this, "Carrinho vazio.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Simula checagem de estoque e baixa
        for(CartItem it: cart.getItems()){
            Product prod = findProductById(it.getProduct().getId());
            if(prod == null || prod.getStock() < it.getQuantity()){
                JOptionPane.showMessageDialog(this, "Estoque insuficiente para " + it.getProduct().getName(), "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        // Baixa estoque
        for(CartItem it: cart.getItems()){
            Product prod = findProductById(it.getProduct().getId());
            prod.setStock(prod.getStock() - it.getQuantity());
        }
        double total = cart.getTotal();
        cart.clear();
        refreshTable();
        updateTotalLabel();
        JOptionPane.showMessageDialog(this, "Compra finalizada! Total: R$ " + df.format(total), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private Product findProductById(int id){
        for(Product p: products) if(p.getId() == id) return p;
        return null;
    }

    private void updateTotalLabel(){
        totalLabel.setText("Total do Carrinho: R$ " + df.format(cart.getTotal()));
    }

    // Botão renderer/editor classes
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(){
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column){
            setText((value==null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox){
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    fireEditingStopped();
                    // ação ao clicar
                    int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
                    Product p = findProductById(id);
                    if(p != null){
                        String qStr = JOptionPane.showInputDialog(ShopSLJ.this, "Quantidade:", "1");
                        if(qStr != null){
                            try{
                                int q = Integer.parseInt(qStr);
                                if(q <= 0){
                                    JOptionPane.showMessageDialog(ShopSLJ.this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                if(q > p.getStock()){
                                    JOptionPane.showMessageDialog(ShopSLJ.this, "Quantidade maior que estoque.", "Erro", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                cart.add(p, q);
                                updateTotalLabel();
                                JOptionPane.showMessageDialog(ShopSLJ.this, "Produto adicionado ao carrinho.");
                            } catch(NumberFormatException ex){
                                JOptionPane.showMessageDialog(ShopSLJ.this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column){
            selectedRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        public Object getCellEditorValue(){
            return label;
        }

        public boolean stopCellEditing(){
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new ShopSLJ().setVisible(true);
        });
    }
}

public class Quantifier {
    private int id;
    private String tipo;
    private int quantidade;

    public Quantifier(int id, String tipo, int quantidade) {
        this.id = id;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public Quantifier(String tipo, int quantidade) {
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", quantidade=" + quantidade +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }
}

public class Pokemon {
    private int id;
    private String nome;
    private String tipo;

    public Pokemon(int id, String nome, String tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
    }

    public Pokemon(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }
}

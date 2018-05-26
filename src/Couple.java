import java.util.Objects;

public class Couple<F, S> {
    private final F first;
    private final S second;

    Couple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public boolean equals(Object o) {
        return (o instanceof Couple) &&
                Objects.equals(first, ((Couple<?,?>)o).first) &&
                Objects.equals(second, ((Couple<?,?>)o).second);
    }

    public int hashCode() {
        return 31 * Objects.hashCode(first) + 17 * Objects.hashCode(second);
    }

    public String toString() {
        return first + ", " + second;
    }
}

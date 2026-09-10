// Anything that can be "caught" by a zombie implements this
public interface Infectable {
    void infect();
    boolean isInfected();
}
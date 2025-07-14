package campeonato.com.Campeonato.exception;

public class ClubeNaoEncontradoException extends RuntimeException {
    public ClubeNaoEncontradoException(String msg) {
        super(msg);
    }
}
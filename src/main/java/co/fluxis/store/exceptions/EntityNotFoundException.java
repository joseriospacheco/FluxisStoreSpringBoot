package co.fluxis.store.exceptions;

public class EntityNotFoundException extends RuntimeException
{
    public EntityNotFoundException(String message)
    {
        super(message);
    }
}

package com.ft.methodeimagesetmapper.exception;

public class TransformationException extends RuntimeException {
    private static final long serialVersionUID = 6664964684696382455L;

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public TransformationException(){
        super();
    }
}

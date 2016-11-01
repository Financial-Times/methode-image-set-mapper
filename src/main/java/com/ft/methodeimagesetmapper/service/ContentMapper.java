package com.ft.methodeimagesetmapper.service;

import com.ft.content.model.Content;
import com.ft.methodeimagesetmapper.model.EomFile;

import java.util.Date;

public interface ContentMapper {

    public Content mapImageSet(String uuid, EomFile eomFile, String transactionId, Date lastModifiedDate);
}

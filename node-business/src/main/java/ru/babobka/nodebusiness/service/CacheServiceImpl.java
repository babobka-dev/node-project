package ru.babobka.nodebusiness.service;


import ru.babobka.nodebusiness.dao.CacheDAO;
import ru.babobka.nodeutils.container.Container;

import java.io.Serializable;


public class CacheServiceImpl implements CacheService {

    private final CacheDAO cacheDAO = Container.getInstance().get(CacheDAO.class);

    @Override
    public void put(String key, Serializable content) {
        cacheDAO.put(key, content);
    }

    @Override
    public <T extends Serializable> T get(String key) {
        return cacheDAO.get(key);
    }

}

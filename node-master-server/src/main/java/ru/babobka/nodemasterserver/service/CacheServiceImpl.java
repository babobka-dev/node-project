package ru.babobka.nodemasterserver.service;

import ru.babobka.nodemasterserver.dao.CacheDAO;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.vsjws.webserver.HttpRequest;
//import ru.babobka.vsjws.webserver.HttpRequest;

public class CacheServiceImpl implements CacheService {

    private final CacheDAO cacheDAO = Container.getInstance().get(CacheDAO.class);

    @Override
    public void putContent(HttpRequest request, String content) {
	cacheDAO.put(request.getUri(), content);

    }

    @Override
    public String getContent(HttpRequest request) {
	return cacheDAO.get(request.getUri());
    }

}

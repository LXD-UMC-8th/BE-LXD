package org.lxdproject.lxd.config.properties;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.ConfigHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.util.ProfileChecker;
import org.lxdproject.lxd.common.util.ProfileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SelectedUrls {

    private final UrlProperties urlProperties;
    private final ProfileChecker profile;

    public String frontend() {
        return pick(urlProperties.getFrontend());
    }

    public String backend() {
        return pick(urlProperties.getBackend());
    }

    private String pick(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new ConfigHandler(ErrorStatus.URL_PROPERTY_MISSING);
        }
        return profile.isLocal() ? urls.get(0) : urls.size() > 1 ? urls.get(1) : urls.get(0);
    }

}
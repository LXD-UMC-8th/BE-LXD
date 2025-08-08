package org.lxdproject.lxd.config.properties;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.ConfigHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.util.ProfileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SelectedUrls {

    private final UrlProperties urlProperties;

    public String frontend() {
        return ProfileUtil.isLocalEnv()
                ? pick(urlProperties.getFrontend(), 0, "urls.frontend")
                : pick(urlProperties.getFrontend(), 1, "urls.frontend");
    }

    public String backend() {
        return ProfileUtil.isLocalEnv()
                ? pick(urlProperties.getBackend(), 0, "urls.backend")
                : pick(urlProperties.getBackend(), 1, "urls.backend");
    }

    private String pick(List<String> list, int idx, String propName) {
        if (list == null || list.isEmpty()) {
            throw new ConfigHandler(ErrorStatus.URL_PROPERTY_MISSING);
        }
        if (idx < list.size()) return list.get(idx);

        return list.get(0);
    }
}
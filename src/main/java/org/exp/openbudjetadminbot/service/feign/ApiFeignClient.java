package org.exp.openbudjetadminbot.service.feign;

import org.exp.openbudjetadminbot.models.dto.response.VoteApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openBudgetApi", url = "https://openbudget.uz/api/v2/info")
public interface ApiFeignClient {

    @GetMapping(value = "/votes/{uuid}", headers = {"Content-Length=0"})
    VoteApiResponse getVotes(
            @PathVariable("uuid") String uuid,
            @RequestParam("page") int page
    );
}

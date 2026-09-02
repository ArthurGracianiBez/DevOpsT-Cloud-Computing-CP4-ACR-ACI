package br.com.api.astraai.clients;

import br.com.api.astraai.clients.dtos.weather.OpenWeatherResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.math.BigDecimal;

@HttpExchange("/data/2.5/weather")
public interface ClimaApiClient {

    @GetExchange
    OpenWeatherResponse getClimaAtual(
            @RequestParam("lat") BigDecimal latitude,
            @RequestParam("lon") BigDecimal longitude,
            @RequestParam("units") String units,
            @RequestParam("appid") String appid
    );
}

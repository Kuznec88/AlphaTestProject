package alphaTestProject.controllers;

import alphaTestProject.model.Exchange;
import alphaTestProject.model.Gif;
import alphaTestProject.services.ExchangeService;
import alphaTestProject.services.GifService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static alphaTestProject.utils.Util.*;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE) // Аннотация для контроллера
public class Controller {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${exchange.server}") private String exServer;       // присваиваем значения для переменных
    @Value("${exchange.app_id}") private String exchangeAppID;
    @Value("${exchange.base}") private String curBase;
    @Value("${gif.server}") private String gifServer;
    @Value("${gif.app_id}") private String gifApiID;
    @Value("${gif.rich}") private String rich;
    @Value("${gif.broken}") private String broken;

    @Autowired
    private final ExchangeService exchangeService;
    @Autowired
    private final GifService gifService;


    public Controller(ExchangeService exchangeService, GifService gifService) {
        this.exchangeService = exchangeService;
        this.gifService = gifService;
    }

    @GetMapping
    public Gif getGif(){
        return getGifAsJSON("RUB");
    }

    @GetMapping("/{currency}")                                      // Здесь присваиваем ответ который должны получить от тега currency
    public Gif getGifAsJSON(@PathVariable String currency) {        // Сюда приходит ответ который написан после тега currency

        Exchange exchangeToday = exchangeService.getExchange(getExchangeURI(exServer, exchangeAppID, curBase, LocalDateTime.now()));
        log.info("get exchangeToday: base currency {} ; rate {} for {} on time {}", exchangeToday.getBase(), exchangeToday.getRates().get(currency.toUpperCase()), currency, LocalDate.now());

        Exchange exchangeYesterday = exchangeService.getExchange(getExchangeURI(exServer,exchangeAppID,curBase,LocalDateTime.now().minusDays(1)));
        log.info("get exchangeYesterday: base currency {} ; rate {} for {} on time {}", exchangeYesterday.getBase(), exchangeYesterday.getRates().get(currency.toUpperCase()), currency, LocalDate.now().minusDays(1));

        String tag = getGifTag(compareExchangeRate(exchangeToday,exchangeYesterday,currency));
        Gif gif = gifService.getGif(getGifURI(gifServer, gifApiID, tag));
        log.info("gifEmbed url {} for {} Tag", gif.getData().getEmbed_url(), tag);

        return gif;
    }

    public String getGifTag(boolean b){
        return b?rich:broken;
    }
}
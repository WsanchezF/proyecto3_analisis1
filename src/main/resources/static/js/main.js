$(document).ready(function() {
    function getExchangeRates() {
        let $usd = $('.exchange-rate-usd');
        let $eur = $('.exchange-rate-eur');

        $.ajax({
            url: 'https://api.apilayer.com/currency_data/live?source=GTQ&currencies=EUR,USD',
            type: 'GET',
            headers: {
                'apikey': 'liYfESGXAVNGvlynh1wREQivtJIdz3oG'
            },
            success: function (response) {
                let usdValue = 1/response.quotes.GTQUSD;
                let eurValue = 1/response.quotes.GTQEUR;

                $usd.html(usdValue.toFixed(2));
                $eur.html(eurValue.toFixed(2));
            },
            error: function(response) {
                console.warn('API rate limit reached, we will show default data now.');
                $usd.html('7.70');
                $eur.html('8.32');
            }
        });
    }

    function initMainPage() {
        getExchangeRates();
    }

    initMainPage();
});
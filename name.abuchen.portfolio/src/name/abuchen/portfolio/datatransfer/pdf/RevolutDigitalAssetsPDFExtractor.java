package name.abuchen.portfolio.datatransfer.pdf;

import static name.abuchen.portfolio.datatransfer.ExtractorUtils.checkAndSetFee;
import static name.abuchen.portfolio.util.TextUtil.trim;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import name.abuchen.portfolio.datatransfer.ExtractorUtils;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Block;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.DocumentType;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Transaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.online.Factory;
import name.abuchen.portfolio.online.SecuritySearchProvider;
import name.abuchen.portfolio.online.impl.CoinGeckoSearchProvider;
import name.abuchen.portfolio.util.TextUtil;

@SuppressWarnings("nls")
public class RevolutDigitalAssetsPDFExtractor extends AbstractPDFExtractor
{
    private static final String EUR = "EUR";

    // preferred CoinGecko coins if a ticker symbol is ambiguous; for
    // example there are multiple coins with the symbol "BTC"
    private static final Map<String, String> PREFERRED_COINS = Map.of("BTC", "bitcoin", //
                    "ETH", "ethereum", //
                    "SOL", "solana", //
                    "DOGE", "dogecoin", //
                    "XRP", "xrp", //
                    "1INCH", "1inch");

    public RevolutDigitalAssetsPDFExtractor(Client client)
    {
        super(client);

        addBankIdentifier("Revolut Digital Assets Europe");
        addBankIdentifier("Individueller Auszug");

        addBuySellTransaction();
    }

    @Override
    public String getLabel()
    {
        return "Revolut Digital Assets Europe Ltd";
    }

    private void addBuySellTransaction()
    {
        final var type = new DocumentType("Individueller Auszug");
        this.addDocumentTyp(type);

        // @formatter:off
        // Transaction statement (only acquisitions)
        // Purchase date Description and symbol Units purchased Unit price Purchase value Fees
        // 05.02.26 DOGE 1,111.1111 €0.08 €84.82 €0.00
        // $0.09 $100.12 $0.00
        // @formatter:on
        var acquisitionBlock = new Block("^[\\d]{2}\\.[\\d]{2}\\.[\\d]{2} [A-Z0-9]{1,10} [\\d]{1,4}(?:,\\d{3})*\\.\\d+ .*€.*$");
        type.addBlock(acquisitionBlock);
        acquisitionBlock.set(new Transaction<BuySellEntry>()

                        .subject(() -> new BuySellEntry(PortfolioTransaction.Type.BUY))

                        .section("date", "tickerSymbol", "shares", "amount", "fee") //
                        .match("^(?<date>[\\d]{2}\\.[\\d]{2}\\.[\\d]{2}) (?<tickerSymbol>[A-Z0-9]{1,10}) (?<shares>[\\d]{1,4}(?:,\\d{3})*\\.\\d+) €[\\d.,]+ €(?<amount>[\\d.,]+) €(?<fee>[\\d.,]+)$") //
                        .assign((t, v) -> {
                            v.put("currency", EUR);

                            var fee = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("fee")));
                            var amount = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("amount")));

                            t.setSecurity(getOrCreateCryptoCurrency(v));

                            t.setDate(parseDate(v.get("date")));
                            t.setShares(asShares(v.get("shares"), "en", "US"));

                            // the purchase value does not include the fee
                            t.setMonetaryAmount(amount.add(fee));
                            checkAndSetFee(fee, t, type.getCurrentContext());
                        })

                        .wrap(BuySellEntryItem::new));

        // @formatter:off
        // Transaction statement (only sales)
        // Date
        // of Sale Description and symbol Age of units Units sold Unit price Value
        // on Sale date of Sale Capital gains Fees
        // of Purchase on Purchase date of Purchase
        // 21.08.26 1INCH 1 year 11 119.93370327 + €0.08 + €9.25 -€15.80 €0.24
        // 01.09.24 months 20 - €0.21 - €25.04 -$16.81 $0.28
        // days + $0.09 + $10.80
        // - $0.23 - $27.61
        // @formatter:on
        var saleBlock = new Block("^[\\d]{2}\\.[\\d]{2}\\.[\\d]{2} [A-Z0-9]{1,10} .*\\+ €.*$");
        type.addBlock(saleBlock);
        saleBlock.setMaxSize(2);
        saleBlock.set(new Transaction<BuySellEntry>()

                        .subject(() -> new BuySellEntry(PortfolioTransaction.Type.SELL))

                        .section("date", "tickerSymbol", "shares", "amount", "fee") //
                        .match("^(?<date>[\\d]{2}\\.[\\d]{2}\\.[\\d]{2}) (?<tickerSymbol>[A-Z0-9]{1,10}) (?<age>.*?) (?<shares>\\d[\\d.,]*) \\+ €[\\d.,]+ \\+ €(?<amount>[\\d.,]+) -?€[\\d.,]+ €(?<fee>[\\d.,]+)$") //
                        .assign((t, v) -> {
                            v.put("currency", EUR);

                            var fee = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("fee")));
                            var amount = Money.of(asCurrencyCode(v.get("currency")), asAmount(v.get("amount")));

                            t.setSecurity(getOrCreateCryptoCurrency(v));

                            t.setDate(parseDate(v.get("date")));
                            t.setShares(asShares(v.get("shares"), "en", "US"));

                            // the sales value is the gross amount; the fee
                            // reduces the proceeds
                            t.setMonetaryAmount(amount.subtract(fee));
                            checkAndSetFee(fee, t, type.getCurrentContext());
                        })

                        .wrap(BuySellEntryItem::new));
    }

    @Override
    protected List<SecuritySearchProvider> lookupCryptoProvider()
    {
        // CoinGecko lookup by symbol can be ambiguous (multiple coins share
        // the same symbol, e.g. several coins use the symbol "BTC"); wrap the
        // provider and keep only the well-known coin for the ambiguous symbols
        var coins = PREFERRED_COINS.entrySet();

        return List.of(new SecuritySearchProvider()
        {
            @Override
            public String getName()
            {
                return "CoinGecko"; //$NON-NLS-1$
            }

            @Override
            public List<SecuritySearchProvider.ResultItem> getCoins() throws IOException
            {
                var filtered = new ArrayList<SecuritySearchProvider.ResultItem>();

                for (var coin : Factory.getSearchProvider(CoinGeckoSearchProvider.class).getCoins())
                {
                    var preferred = PREFERRED_COINS.get(coin.getSymbol().toUpperCase(Locale.ROOT));
                    if (preferred == null || preferred.equalsIgnoreCase(coin.getName()))
                        filtered.add(coin);
                }

                return filtered;
            }

            @Override
            public List<SecuritySearchProvider.ResultItem> search(String query) throws IOException
            {
                return List.of();
            }
        });
    }

    private java.time.LocalDateTime parseDate(String value)
    {
        // @formatter:off
        // dates are printed with a two digit year: 05.02.26
        // @formatter:on
        return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd.MM.yy")).atStartOfDay();
    }

    @Override
    protected long asAmount(String value)
    {
        // amounts use the US number format: €54,000.05
        return ExtractorUtils.convertToNumberLong(value, Values.Amount,
                        ExtractorUtils.guessNumberLocale(value, Locale.US));
    }
}

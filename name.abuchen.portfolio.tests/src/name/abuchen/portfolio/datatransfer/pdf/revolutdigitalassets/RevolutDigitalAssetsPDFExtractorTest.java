package name.abuchen.portfolio.datatransfer.pdf.revolutdigitalassets;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasAmount;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasCurrencyCode;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasDate;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasExDate;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasFeed;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasFeedProperty;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasFees;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasGrossValue;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasIsin;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasName;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasNote;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasShares;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasSource;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasTicker;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasTaxes;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasWkn;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.purchase;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.sale;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.security;

import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countAccountTransactions;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countAccountTransfers;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countBuySell;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countItemsWithFailureMessage;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countSecurities;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countSkippedItems;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.datatransfer.pdf.RevolutDigitalAssetsPDFExtractor;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.online.impl.CoinGeckoQuoteFeed;
import name.abuchen.portfolio.online.SecuritySearchProvider;
import name.abuchen.portfolio.datatransfer.pdf.TestCoinSearchProvider;

public class RevolutDigitalAssetsPDFExtractorTest
{
    private RevolutDigitalAssetsPDFExtractor extractor = new RevolutDigitalAssetsPDFExtractor(new Client())
    {
        @Override
        protected List<SecuritySearchProvider> lookupCryptoProvider()
        {
            return TestCoinSearchProvider.cryptoProvider();
        }
    };

    @Test
    public void testIndividuellerAuszug01()
    {
        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "IndividuellerAuszug01.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(3L));
        assertThat(countBuySell(results), is(6L));
        assertThat(countAccountTransactions(results), is(0L));
        assertThat(countAccountTransfers(results), is(0L));
        assertThat(countItemsWithFailureMessage(results), is(0L));
        assertThat(countSkippedItems(results), is(0L));
        assertThat(results.size(), is(9));
        new AssertImportActions().check(results, "EUR");

        // check securities
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("BTC"), //
                        hasName("Bitcoin"), //
                        hasCurrencyCode("EUR"), //
                        hasFeed(CoinGeckoQuoteFeed.ID), //
                        hasFeedProperty(CoinGeckoQuoteFeed.COINGECKO_COIN_ID, "bitcoin"))));
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("ETH"), //
                        hasName("Ethereum"), //
                        hasCurrencyCode("EUR"), //
                        hasFeed(CoinGeckoQuoteFeed.ID), //
                        hasFeedProperty(CoinGeckoQuoteFeed.COINGECKO_COIN_ID, "ethereum"))));
        assertThat(results, hasItem(security( //
                        hasIsin(null), hasWkn(null), hasTicker("XRP"), //
                        hasName("XRP"), //
                        hasCurrencyCode("EUR"), //
                        hasFeed(CoinGeckoQuoteFeed.ID), //
                        hasFeedProperty(CoinGeckoQuoteFeed.COINGECKO_COIN_ID, "xrp"))));

        // check buy transactions
        assertThat(results, hasItem(purchase( //
                        hasDate("2026-02-05"), hasShares(0.00222222), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 120.00), hasGrossValue("EUR", 120.00), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 0.00))));
        assertThat(results, hasItem(purchase( //
                        hasDate("2026-02-23"), hasShares(0.06329113), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 100.00), hasGrossValue("EUR", 100.00), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 0.00))));
        assertThat(results, hasItem(purchase( //
                        hasDate("2026-02-24"), hasShares(0.00187357), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 100.00), hasGrossValue("EUR", 100.00), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 0.00))));
        assertThat(results, hasItem(purchase( //
                        hasDate("2026-05-27"), hasShares(171.719136), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 202.98), hasGrossValue("EUR", 200.00), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 2.98))));

        // check sell transactions
        assertThat(results, hasItem(sale( //
                        hasDate("2026-08-21"), hasShares(0.00123456), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 66.43), hasGrossValue("EUR", 66.67), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 0.24))));

        // check sell transaction with gain (purchase value without minus sign)
        assertThat(results, hasItem(sale( //
                        hasDate("2026-08-22"), hasShares(0.5), //
                        hasSource("IndividuellerAuszug01.txt"), //
                        hasNote(null), //
                        hasAmount("EUR", 1500.00), hasGrossValue("EUR", 1500.00), //
                        hasTaxes("EUR", 0.00), hasFees("EUR", 0.00))));
    }
}

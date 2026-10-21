package ltu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PaymentTest
{
    private static final String LOAN_ONLY_RULES =
        "student100loan=7088\nstudent100subsidy=0\nstudent50loan=3564\nstudent50subsidy=0\n" +
        "student0loan=0\nstudent0subsidy=0\nfulltimeIncome=85813\nparttimeIncome=128722\n";

    private static final String SUBSIDY_ONLY_RULES =
        "student100loan=0\nstudent100subsidy=2816\nstudent50loan=0\nstudent50subsidy=1396\n" +
        "student0loan=0\nstudent0subsidy=0\nfulltimeIncome=85813\nparttimeIncome=128722\n";

    private PaymentImpl payment(String yyyyMMdd) throws Exception
    {
        return new PaymentImpl(new FakeCalendar(yyyyMMdd));
    }

    // Payment with custom rules, used to isolate loan from subsidy.
    private PaymentImpl payment(String yyyyMMdd, String rules) throws Exception
    {
        return new PaymentImpl(new FakeCalendar(yyyyMMdd), rules);
    }

    private static String personId(int birthYear)
    {
        return String.format("%d1234-1234", birthYear);
    }

    // Study pace requirements
    @Test
    public void req201_studyRateBelow50GetsNoSubsidy() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1991), 0, 49, 100));
    }

    @Test
    public void req202_halfTimeStudyGetsHalfSubsidy() throws Exception
    {
        assertEquals(4960, payment("20160315").getMonthlyAmount(personId(1991), 0, 50, 100));
    }

    @Test
    public void req202_justBelowFullTimeGetsHalfSubsidy() throws Exception
    {
        assertEquals(4960, payment("20160315").getMonthlyAmount(personId(1991), 0, 99, 100));
    }

    @Test
    public void req203_fullTimeStudyGetsFullSubsidy() throws Exception
    {
        assertEquals(9904, payment("20160315").getMonthlyAmount(personId(1991), 0, 100, 100));
    }

    // Income limits
    @Test
    public void req301_fullTimeIncomeAtLimitGetsAmount() throws Exception
    {
        assertEquals(9904, payment("20160315").getMonthlyAmount(personId(1991), 85813, 100, 100));
    }

    @Test
    public void req301_fullTimeIncomeOverLimitGetsNothing() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1991), 85814, 100, 100));
    }

    @Test
    public void req302_partTimeIncomeAtLimitGetsAmount() throws Exception
    {
        assertEquals(4960, payment("20160315").getMonthlyAmount(personId(1991), 128722, 50, 100));
    }

    @Test
    public void req302_partTimeIncomeOverLimitGetsNothing() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1991), 128723, 50, 100));
    }

    // Completion ratio requirement
    @Test
    public void req401_completionBelow50GetsNothing() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1991), 0, 100, 49));
    }

    @Test
    public void req401_completionAt50GetsAmount() throws Exception
    {
        assertEquals(9904, payment("20160315").getMonthlyAmount(personId(1991), 0, 100, 50));
    }

    // Age requirements
    @Test
    public void age19GetsNothing() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1997), 0, 100, 100));
    }

    @Test
    public void age20GetsFullAmount() throws Exception
    {
        assertEquals(9904, payment("20160315").getMonthlyAmount(personId(1996), 0, 100, 100));
    }

    @Test
    public void age46GetsFullAmount() throws Exception
    {
        assertEquals(9904, payment("20160315").getMonthlyAmount(personId(1970), 0, 100, 100));
    }

    @Test
    public void age47GetsSubsidyOnly() throws Exception
    {
        assertEquals(2816, payment("20160315").getMonthlyAmount(personId(1969), 0, 100, 100));
    }

    @Test
    public void age56GetsSubsidyOnly() throws Exception
    {
        assertEquals(2816, payment("20160315").getMonthlyAmount(personId(1960), 0, 100, 100));
    }

    @Test
    public void age57GetsNothing() throws Exception
    {
        assertEquals(0, payment("20160315").getMonthlyAmount(personId(1959), 0, 100, 100));
    }

    // Paid amounts
    @Test
    public void req501_req505_fullTimeLoanIs7088() throws Exception
    {
        assertEquals(7088, payment("20160315", LOAN_ONLY_RULES).getMonthlyAmount(personId(1991), 0, 100, 100));
    }

    @Test
    public void req503_req505_halfTimeLoanIs3564() throws Exception
    {
        assertEquals(3564, payment("20160315", LOAN_ONLY_RULES).getMonthlyAmount(personId(1991), 0, 50, 100));
    }

    @Test
    public void req502_fullTimeSubsidyIs2816() throws Exception
    {
        assertEquals(2816, payment("20160315", SUBSIDY_ONLY_RULES).getMonthlyAmount(personId(1991), 0, 100, 100));
    }

    @Test
    public void req504_halfTimeSubsidyIs1396() throws Exception
    {
        assertEquals(1396, payment("20160315", SUBSIDY_ONLY_RULES).getMonthlyAmount(personId(1991), 0, 50, 100));
    }

    // Payment day
    @Test
    public void req506_januaryPaymentDayIsLastFriday() throws Exception
    {
        assertEquals("20160129", payment("20160115").getNextPaymentDay());
    }

    @Test
    public void req506_februaryPaymentDayIsLastWeekday() throws Exception
    {
        assertEquals("20160229", payment("20160215").getNextPaymentDay());
    }

    @Test
    public void req506_marchPaymentDayIsLastWeekday() throws Exception
    {
        assertEquals("20160331", payment("20160315").getNextPaymentDay());
    }

    @Test
    public void req506_aprilPaymentDayIsLastFriday() throws Exception
    {
        assertEquals("20160429", payment("20160415").getNextPaymentDay());
    }

    // Invalid input handling
    @Test(expected = IllegalArgumentException.class)
    public void nullPersonIdThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount(null, 0, 100, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeIncomeThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount(personId(1991), -1, 100, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeStudyRateThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount(personId(1991), 0, -1, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeCompletionRatioThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount(personId(1991), 0, 100, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooShortPersonIdThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount("19960315-12", 0, 100, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongPersonIdThrows() throws Exception
    {
        payment("20160315").getMonthlyAmount("19960315-12345", 0, 100, 100);
    }
}
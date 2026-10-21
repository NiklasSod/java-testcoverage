package ltu;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FakeCalendar implements ICalendar
{
    private final Date date;

    public FakeCalendar(String yyyyMMdd) throws Exception
    {
        date = new SimpleDateFormat("yyyyMMdd").parse(yyyyMMdd);
    }

    @Override
    public Date getDate()
    {
        return date;
    }
}
/*
 * This file is generated by jOOQ.
 */
package my.company.app.generated.jooq;


import my.company.app.generated.jooq.tables.Session;
import my.company.app.generated.jooq.tables.User;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.session</code>.
     */
    public static final Session SESSION = my.company.app.generated.jooq.tables.Session.SESSION;

    /**
     * The table <code>public.user</code>.
     */
    public static final User USER = my.company.app.generated.jooq.tables.User.USER;
}

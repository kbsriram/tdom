package org.tdom;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import static org.tdom.TDom.*;

public class BasicTest
{
    private final void check(TNode base, String expected)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        base.dump(pw);
        pw.close();
        assertEquals(expected, sw.toString());
    }

    @Test public void testBasicOperations()
    {
        PrintWriter pw = new PrintWriter(System.out);

        TNode html =
            n("html",
              n("head",
                n("link", a("type", "stylesheet"), a("href", "a.css"))));

        check(html,
              "<html><head><link type=\"stylesheet\" href=\"a.css\" />"+
              "</head></html>");

        TNode body =
              n("body",
                n("h1",
                  t("Hello, world")),
                n("div", a("class", "content")));
        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "</div></body>");

        body.append
            (".content",
             n("div", a("class", "tile"), t("This is tile 0")));

        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<div class=\"tile\">This is tile 0</div></div></body>");

        html.after("head", body);
        check(html,
              "<html><head><link type=\"stylesheet\" href=\"a.css\" /></head>"+
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<div class=\"tile\">This is tile 0</div></div></body></html>");

        // Should not be able to reparent body.
        try {
            html.append(body);
            fail("Did not catch reparenting attempt");
        }
        catch (IllegalStateException ise) {
            // ok
        }

        html.before
            (".tile", n("h2", t("Tile header")));

        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<h2>Tile header</h2><div class=\"tile\">"+
              "This is tile 0</div></div></body>");

        html.after
            (".tile", n("h2", a("class", "footer"), t("a footer")));

        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<h2>Tile header</h2><div class=\"tile\">"+
              "This is tile 0</div><h2 class=\"footer\">a footer</h2>"+
              "</div></body>");

        html.before
            ("h2", n("p", a("class", "prefooter"), t("a pre-footer")));

        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<p class=\"prefooter\">a pre-footer</p>"+
              "<h2>Tile header</h2><div class=\"tile\">This is tile 0</div>"+
              "<p class=\"prefooter\">a pre-footer</p>"+
              "<h2 class=\"footer\">a footer</h2></div></body>");

        html.remove("p.prefooter");

        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<h2>Tile header</h2><div class=\"tile\">This is tile 0</div>"+
              "<h2 class=\"footer\">a footer</h2></div></body>");

        html.remove("h2");
        check(body,
              "<body><h1>Hello, world</h1><div class=\"content\">"+
              "<div class=\"tile\">This is tile 0</div></div></body>");

        html.remove(".content");
        check(body,
              "<body><h1>Hello, world</h1></body>");
    }
}

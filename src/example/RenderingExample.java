import java.io.PrintWriter;

import org.tdom.TDom;
import static org.tdom.TDom.*;

// You can use a custom visitor to generate various textual formats
// from the same data structure. This can useful when you want to
// return (say) html, xml and json from the same underlying data
// structure.

// The example creates a sample HTML snippet with some embedded hCard
// microformat elements. In addition to rendering to html, it selects
// the embedded elements and renders them into vcard format.

public class RenderingExample
{
    public static void main(String args[])
    {
        PrintWriter printWriter = new PrintWriter(System.out);

        TNode html =
            n("html",
              n("head",
                n("title", t("A title"))),
              n("body",
                n("h1", t("This is the info for a user"))));

        // We add some content, marked up with some hCard tags.
        // If you wrote it directly in HTML, this is how it looks.

        // <div class="vcard">
        //   <img class="photo" src="http://example.com/bob.jpg"/>
        //   <strong class="fn">Bob Smith</strong>
        //   is the
        //   <span class="title">Senior editor</span>
        //   at
        //   <span class="org">ACME Reviews</span>
        // </div>

        TNode content =
            n("div", a("class", "vcard"),
              n("img", a("class", "photo"), a("src", "http://example.com/bob.jpg")),
              n("strong", a("class", "fn"), t("Bob Smith")),
              t(" is the "),
              n("span", a("class", "title"), t("Senior editor")),
              t(" at "),
              n("span", a("class", "org"), t("ACME Reviews")));

        // Append the content to the body, and add a footer.
        html.append("body", content)
            .append("body", n("h3", t("This is a footer")));

        // Render out to html.
        html.dump(printWriter);
        printWriter.println();

        // Render out the vcard to a vcard format.
        html.select(".vcard").visit(new VCardRenderer(printWriter));
        printWriter.println();
        printWriter.flush();
    }

    // Simple-minded implementation just for a demo.
    private final static class VCardRenderer implements TVisitor
    {
        public void visitText(TText t)
        { m_pw.print(t.getText()); }

        public void visitAttr(TAttr attr) {}

        public void visitTagNode(TTagNode n)
        {
            TAttr attr = n.getAttrs().get("class");
            String vtag;
            if (attr != null) { vtag = attr.getValue(); }
            else { vtag = null; }

            // Print vcard line as appropriate.
            if (vtag != null) {
                if (vtag.equals("vcard")) {
                    m_pw.println("BEGIN:VCARD");
                    m_pw.println("VERSION:4.0");
                    for (TDom child: n.getChildren()) {
                        if (child instanceof TTagNode) { child.visit(this); }
                    }
                    m_pw.println("END:VCARD");
                }

                else if (vtag.equals("photo")) {
                    m_pw.print("PHOTO:");
                    m_pw.println(n.getAttrs().get("src").getValue());
                }

                else if (vtag.equals("fn") ||
                    vtag.equals("title") ||
                    vtag.equals("org")) {
                    m_pw.print(vtag.toUpperCase());
                    m_pw.print(":");
                    for (TDom child: n.getChildren()) {
                        if (child instanceof TText) { child.visit(this); }
                    }
                    m_pw.println();
                }
                return;
            }

            // default: just descend to all the child nodes.
            for (TDom child: n.getChildren()) {
                if (child instanceof TTagNode) { child.visit(this); }
            }
        }

        public void visitList(TList l)
        {
            for (TTagNode child: l.getEntries()) {
                child.visit(this);
            }
        }

        private VCardRenderer(PrintWriter pw)
        { m_pw = pw; }
        private final PrintWriter m_pw;
    }

    private final static class JSONRenderer implements TVisitor
    {
        public void visitText(TText t){}
        public void visitAttr(TAttr attr){}
        public void visitTagNode(TTagNode n){}
        public void visitList(TList l){}

        private JSONRenderer(PrintWriter pw)
        { m_pw = pw; }
        private final PrintWriter m_pw;
    }
}

/**
 * <p>This package is a standalone library to create a simple tree
 * datastructure that can be created and modified succinctly, and
 * rendered into HTML or other textual formats.</p>
 *
 * <p>Here's a hello world example.</p>
 *
 * <pre> {@code
 * import org.tdom.TDom;
 * import org.tdom.TDom.*;
 *
 * // Create straight-up html 
 * TNode html = n("html",
 *                n("head",
 *                  n("title", t("A title"))),
 *                n("body",
 *                  n("div", a("class", "content"),
 *                    t("Hello, world."))));
 *
 * // Create a header separately.
 * TNode header = n("h1", t("My title"));
 * 
 * // Insert the header before the content.
 * html.before(".content", header);
 *
 * // Add another block of text to the body.
 * html.append("body", n("div", t("Goodbye, world.")));
 *
 * // Insert a ruler after both divs.
 * html.after("div", n("hr"));
 *
 * // methods can be chained.
 * html
 *   .append("body", n("div", a("class", "footer"), t("a footer")))
 *   .after(".footer", t("that's all folks!"));
 *
 *
 * html.dump(new PrintWriter(System.out));
 * }</pre>
 *
 * <p>TDom has three creation methods,
 * {@link org.tdom.TDom#n(java.lang.String, org.tdom.TDom...)} to create
 * nodes, {@link org.tdom.TDom#t(Object)} to create text, and
 * {@link org.tdom.TDom#a(String,Object)} to create attributes. These may be succintly
 * created as in the example above, or they may be added to individual
 * nodes using methods like
 * {@link org.tdom.TDom.TNode#append(org.tdom.TDom) }</p>
 *
 * <p>In addition, there are methods (rather like jquery) to add nodes
 * relative to a set of other nodes. A very simple selector syntax
 * is available. <tt>tag.class</tt> selects nodes with the given tag
 * and class. The <tt>tag</tt> or <tt>class</tt>may be left empty. The
 * <tt>#id</tt> selects the node with the given id.,</p>
 *
 * <p>Using this syntax, you may add nodes more precisely into the tree
 * with the
 * {@link org.tdom.TDom.TNode#append(String, org.tdom.TDom) },
 * {@link org.tdom.TDom.TNode#prepend(String, org.tdom.TDom) },
 * {@link org.tdom.TDom.TNode#before(String, org.tdom.TDom) } and
 * {@link org.tdom.TDom.TNode#after(String, org.tdom.TDom)} methods.</p>
 *
 * <p>Note that there are
 * always variations of these methods that don't use a selector, in which
 * case they are assumed to apply to the current node.</p>
 *
 * <p>TDom generally does not care about the name used for the tags, so
 * you can use it to generate xml formats. Finally, TDom being a data
 * structure rather than markup, you can use your own
 * visitor {@link org.tdom.TDom.TVisitor} to render the resultant tree.
 * TDom itself
 * provides a simple HTML visitor, used when you call
 * {@link org.tdom.TDom#dump(PrintWriter)}.</p>
 */

package org.tdom;

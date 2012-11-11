package org.tdom;

/**
 * This is the base class that implements a succint dom-like
 * datastructure that's convenient to create, and render into
 * various textual formats.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import java.io.PrintWriter;

public abstract class TDom
{
    public final static TNode n(String name, TDom... extra)
    {
        TNode ret = new TNode(name);
        for (int i=0; i<extra.length; i++) {
            ret.append(extra[i]);
        }
        return ret;
    }

    public final static TAttr a(String name, String value)
    { return new TAttr(name, value); }

    public final static TText t(String text)
    { return new TText(text); }

    public void dump(final PrintWriter pw)
    { visit(new HTMLVisitor(pw)); }

    public interface TVisitor
    {
        void visitText(TText t);
        void visitAttr(TAttr attr);
        void visitNode(TNode n);
    }

    public abstract void visit(TVisitor v);
    public abstract TDom dup();

    public final static class TText extends TDom
    {
        public TText(String text)
        { m_text = text; }
        public String getText()
        { return m_text; }
        public void visit(TVisitor v)
        { v.visitText(this); }
        public TText dup()
        { return this; }

        private final String m_text;
    }

    public final static class TAttr extends TDom
    {
        public TAttr(String name, String value)
        {
            m_name = name;
            m_value = value;
        }
        public String getName()
        { return m_name; }
        public String getValue()
        { return m_value; }
        public void visit(TVisitor v)
        { v.visitAttr(this); }
        public TAttr dup()
        { return this; }

        private final String m_name;
        private final String m_value;
    }

    public final static class TNode extends TDom
    {
        public TNode(String name)
        { m_name = name; }

        public void setParent(TNode n)
        {
            if (m_parent != null) {
                throw new IllegalStateException
                    ("Cannot reparent <"+getName()+">");
            }
            m_parent = n;
        }
        public TNode up()
        { return m_parent; }

        public TNode dup()
        {
            TNode ret = new TNode(m_name);
            for (TAttr attr: m_attrs.values()) {
                ret.append(attr.dup());
            }
            for (TDom child: m_children) {
                ret.append(child.dup());
            }
            return ret;
        }

        public TNode before(TDom thing)
        {
            if (m_parent == null) {
                throw new IllegalStateException
                    ("No parent for <"+getName()+">");
            }

            List<TDom> siblings = m_parent.m_children;
            for (int i=siblings.size()-1; i>=0; i--) {
                if (siblings.get(i) == this) {
                    m_parent.addAt(i, thing);
                    return this;
                }
            }
            // This is unexpected.
            throw new IllegalStateException
                ("Unexpected -- parent doesn't contain child.");
        }

        public TNode before(String selector, TDom thing)
        { return before(false, selector, thing); }

        public TNode before(boolean throwifmissing, String selector, TDom thing)
        {
            return doOp(throwifmissing, selector, thing, new Op() {
                    public void apply(TNode target, TDom d) {
                        target.before(d);
                    }
                });
        }

        public TNode after(TDom thing)
        {
            if (m_parent == null) {
                throw new IllegalStateException
                    ("No parent for <"+getName()+">");
            }

            List<TDom> siblings = m_parent.m_children;
            for (int i=siblings.size()-1; i>=0; i--) {
                if (siblings.get(i) == this) {
                    if (i == (siblings.size()-1)) {
                        m_parent.addAt(-1, thing);
                    }
                    else {
                        m_parent.addAt(i+1, thing);
                    }
                    return this;
                }
            }
            // This is unexpected.
            throw new IllegalStateException
                ("Unexpected -- parent doesn't contain child.");
        }

        public TNode after(String selector, TDom thing)
        { return after(false, selector, thing); }

        public TNode after(boolean throwifmissing, String selector, TDom thing)
        {
            return doOp(throwifmissing, selector, thing, new Op() {
                    public void apply(TNode target, TDom d) {
                        target.after(d);
                    }
                });
        }


        public TNode addAt(int idx, TDom thing)
        {
            if (thing instanceof TAttr) {
                TAttr attr = (TAttr) thing;
                m_attrs.put(attr.getName().toLowerCase(), attr);
            }
            else {
                // First ensure we can reparent.
                if (thing instanceof TNode) {
                    ((TNode) thing).setParent(this);
                }
                if (idx >= 0) {
                    m_children.add(idx, thing);
                }
                else {
                    m_children.add(thing);
                }
            }
            return this;
        }

        public TNode remove(TDom thing)
        {
            if (thing instanceof TAttr) {
                m_attrs.remove(((TAttr) thing).getName().toLowerCase());
                return this;
            }
            m_children.remove(thing);
            return this;
        }
        public TNode remove(String selector)
        { return remove(false, selector); }

        public TNode remove(boolean throwifmissing, String selector)
        {
            return doOp(throwifmissing, selector, null, new Op() {
                    public void apply(TNode target, TDom d) {
                        if (target.m_parent != null) {
                            target.m_parent.remove(target);
                        }
                    }
                });
        }

        public TNode append(TDom thing)
        { return addAt(-1, thing); }

        public TNode append(String selector, TDom thing)
        { return append(false, selector, thing); }

        public TNode append(boolean throwifmissing, String selector, TDom thing)
        {
            return doOp(throwifmissing, selector, thing, new Op() {
                    public void apply(TNode target, TDom d) {
                        target.append(d);
                    }
                });
        }

        private TNode doOp
            (boolean throwifmissing, String selector, TDom thing, Op op)
        {
            int count = 0;
            for (TNode node: find(selector)) {
                if (count == 0) { op.apply(node, thing); }
                else { op.apply(node, (thing!=null)?thing.dup():null); }
                count++;
            }
            if (throwifmissing && (count == 0)) {
                throw new NullPointerException
                    ("No node matched '"+selector+ "' under <"+getName()+">");
            }
            return this;
        }

        public TNode prepend(TDom thing)
        { return addAt(0, thing); }

        public TNode prepend(String selector, TDom thing)
        { return prepend(false, selector, thing); }

        public TNode prepend(boolean throwifmissing,String selector, TDom thing)
        {
            return doOp(throwifmissing, selector, thing, new Op() {
                    public void apply(TNode target, TDom d) {
                        target.prepend(d);
                    }
                });
        }

        public List<TNode> find(String selector)
        {
            int idx = selector.indexOf('.');
            List<TNode> ret = new ArrayList<TNode>();
            if (idx >= 0) {
                String tag = (idx==0)?null:selector.substring(0, idx);
                String clazz = (idx<(selector.length()-1))?
                    selector.substring(idx+1):null;
                return findByTagClass(tag, clazz, ret);
            }
            else if (selector.startsWith("#")) {
                TNode node = findById(selector.substring(1));
                if (node != null) {
                    ret.add(node);
                }
            }
            else {
                return findByTagClass(selector, null, ret);
            }
            return ret;
        }

        public boolean matchAttr(String name, String value)
        {
            TAttr attr = m_attrs.get(name);
            if (attr == null) { return false; }
            return value.equals(attr.getValue());
        }

        public List<TNode> findByTagClass(String tag, String clazz)
        { return findByTagClass(tag, clazz, new ArrayList<TNode>()); }

        public List<TNode> findByTagClass
            (String tag, String clazz, List<TNode> accum)
        {
            if (((tag == null) || (getName().equals(tag))) &&
                ((clazz == null) || (matchAttr("class", clazz)))) {
                accum.add(this);
                return accum;
            }

            for (TDom child: m_children) {
                if (child instanceof TNode) {
                    ((TNode) child).findByTagClass(tag, clazz, accum);
                }
            }
            return accum;
        }

        public TNode findById(String id)
        {
            if (matchAttr("id", id)) {
                return this;
            }

            for (TDom child: m_children) {
                if (child instanceof TNode) {
                    TNode ret = ((TNode) child).findById(id);
                    if (ret != null) { return ret; }
                }
            }
            return null;
        }

        public String getName()
        { return m_name; }
        public Map<String,TAttr> getAttrs()
        { return m_attrs; }
        public List<TDom> getChildren()
        { return m_children; }
        public void visit(TVisitor v)
        { v.visitNode(this); }

        private final String m_name;
        private TNode m_parent = null;
        private final List<TDom> m_children = new ArrayList<TDom>();
        private final Map<String,TAttr> m_attrs = new HashMap<String,TAttr>();
        interface Op { void apply(TNode target, TDom thing); }
    }

    private final static class HTMLVisitor
        implements TVisitor
    {
        HTMLVisitor(PrintWriter pw)
        { m_pw = pw; }

        public void visitText(TText t)
        { m_pw.print(t.getText()); }
        public void visitAttr(TAttr attr)
        {
            m_pw.print(attr.getName());
            m_pw.print("=\"");
            m_pw.print(attr.getValue());
            m_pw.print("\"");
        }
        public void visitNode(TNode n)
        {
            m_pw.print("<");
            m_pw.print(n.getName());
            for (TAttr attr: n.getAttrs().values()) {
                m_pw.print(" ");
                attr.visit(this);
            }

            List<TDom> children = n.getChildren();
            if (children.size() == 0) {
                if (NO_ABBREV.contains(n.getName())) {
                    m_pw.print("></"+n.getName()+">");
                }
                else {
                    m_pw.print(" />");
                }
            }
            else {
                m_pw.print(">");
                for (TDom child: children) {
                    child.visit(this);
                }
                m_pw.print("</");
                m_pw.print(n.getName());
                m_pw.print(">");
            }
        }

        private final PrintWriter m_pw;
        private final static Set<String> NO_ABBREV;
        static
        {
            NO_ABBREV = new HashSet<String>();
            NO_ABBREV.add("div");
            NO_ABBREV.add("a");
            NO_ABBREV.add("script");
        }
    }
}

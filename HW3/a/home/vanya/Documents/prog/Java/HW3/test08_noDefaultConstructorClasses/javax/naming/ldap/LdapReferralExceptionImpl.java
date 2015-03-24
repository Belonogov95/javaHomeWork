package javax.naming.ldap;

public class LdapReferralExceptionImpl extends javax.naming.ldap.LdapReferralException {
    protected LdapReferralExceptionImpl () {
        super();
    }
    protected LdapReferralExceptionImpl (java.lang.String a0) {
        super(a0);
    }
    public javax.naming.Context getReferralContext() {
        return null;
    }
    public boolean skipReferral() {
        return false;
    }
    public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> a0) {
        return null;
    }
    public java.lang.Object getReferralInfo() {
        return null;
    }
    public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> a0, javax.naming.ldap.Control[] a1) {
        return null;
    }
    public void retryReferral() {}
}

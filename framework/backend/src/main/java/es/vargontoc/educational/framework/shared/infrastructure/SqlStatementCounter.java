package es.vargontoc.educational.framework.shared.infrastructure;

public interface SqlStatementCounter {

    long snapshot();

    long countSince(long snapshot);

    SqlStatementCounter NOOP = new SqlStatementCounter() {
        @Override
        public long snapshot() {
            return 0;
        }

        @Override
        public long countSince(long snapshot) {
            return 0;
        }
    };
}

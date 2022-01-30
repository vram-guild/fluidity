package grondag.fluidity.test;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.impl.TransactionImpl;

class TransactionTest {
	TestParticipant p0;
	TestParticipant p1;
	TestParticipant p2;

	@BeforeAll
	static void init() {
		TransactionImpl.setServerThread(Thread.currentThread());
	}

	@BeforeEach
	public void setup() {
		p0 = new TestParticipant("");
		p1 = new TestParticipant("");
		p2 = new TestParticipant("");
		setAll("a");
		assertAll("a");
	}

	void setAll(String val) {
		p0.value(val);
		p1.value(val);
		p2.value(val);
	}

	void assertAll(String val) {
		assert p0.value().equals(val);
		assert p1.value().equals(val);
		assert p2.value().equals(val);
	}

	@Test
	void commit() {
		try (var tx = Transaction.open()) {
			setAll("b");
			tx.commit();
		}

		assertAll("b");
	}

	@Test
	void explicitRollback() {
		try (var tx = Transaction.open()) {
			setAll("b");
			tx.rollback();
		}

		assertAll("a");
	}

	@Test
	void implicitRollback() {
		try (var tx = Transaction.open()) {
			setAll("b");
		}

		assertAll("a");
	}

	@Test
	void nestedCommit() {
		try (var tx0 = Transaction.open()) {
			p0.value("b");

			try (var tx1 = Transaction.open()) {
				p1.value("b");

				try (var tx2 = Transaction.open()) {
					p2.value("b");
					tx2.commit();
				}

				tx1.commit();
			}

			tx0.commit();
		}

		assertAll("b");
	}

	@Test
	void commitRollbackCommit() {
		try (var tx0 = Transaction.open()) {
			p0.value("b");

			try (var tx1 = Transaction.open()) {
				p1.value("c");
				p0.value("c");

				try (var tx2 = Transaction.open()) {
					p0.value("d");
					p1.value("d");
					p2.value("d");
					tx2.commit();
				}

				tx1.rollback();
			}

			tx0.commit();
		}

		assertTrue(p0.value().contentEquals("b"));
		assertTrue(p1.value().contentEquals("a"));
		assertTrue(p2.value().contentEquals("a"));
	}

	@Test
	void rollbackCommitCommit() {
		try (var tx0 = Transaction.open()) {
			p0.value("b");

			try (var tx1 = Transaction.open()) {
				p1.value("c");
				p0.value("c");

				try (var tx2 = Transaction.open()) {
					p0.value("d");
					p1.value("d");
					p2.value("d");
					tx2.commit();
				}

				tx1.commit();
			}

			tx0.rollback();
		}

		assertTrue(p0.value().contentEquals("a"));
		assertTrue(p1.value().contentEquals("a"));
		assertTrue(p2.value().contentEquals("a"));
	}

	@Test
	void commitCommitCommit() {
		try (var tx0 = Transaction.open()) {
			p0.value("b");

			try (var tx1 = Transaction.open()) {
				p1.value("c");
				p0.value("c");

				try (var tx2 = Transaction.open()) {
					p0.value("d");
					p1.value("d");
					p2.value("d");
					tx2.commit();
				}

				tx1.commit();
			}

			tx0.commit();
		}

		assertTrue(p0.value().contentEquals("d"));
		assertTrue(p1.value().contentEquals("d"));
		assertTrue(p2.value().contentEquals("d"));
	}
}

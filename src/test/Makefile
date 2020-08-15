PROJECTROOT = ..
RMIREGPORT = 3345

clean :
	rm -rf ./data/*
	rm -rf ./results/*
	$(RM) *.class

all :
	javac -classpath $(PROJECTROOT) *.java

test :
##Basic Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=BasicXid test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=BasicBind test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=BasicCommit test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=BasicAbort test.TestManager
#
##CRUD Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDCreate test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDRead test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDCreateFailInvalidItem test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDCreateFailParam test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDDelete test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDDeleteFailParam test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDDeleteFailSeq test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=CRUDUpdate test.TestManager
#Die Test
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRM test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRMAfterEnlist test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRMBeforePrepare test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRMAfterPrepare test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRMBeforeAbort test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieRMBeforeCommit test.TestManager

	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieTM test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieTMBeforeCommit test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieTMAfterCommit test.TestManager

	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=ACIDDieWC test.TestManager

#
#Lock Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockDead test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockDead2 test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockIsolation test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockRR test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockRW test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockWW test.TestManager
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestClass=LockWR test.TestManager










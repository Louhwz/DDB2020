RMIREGPORT =3345
PROJECTROOT = ..
all : server client

server : ./rm/ResourceManager.class \
         ./rm/ResourceManagerImpl.class \
         TransactionManager.class \
         TransactionManagerImpl.class \
         WorkflowController.class \
         WorkflowControllerImpl.class \
         ./exception/InvalidTransactionException.class \
         ./exception/TransactionAbortedException.class

client: Client.class

runregistry :
	rmiregistry -J-classpath -J$(PROJECTROOT) $(RMIREGPORT)

runtm : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.TransactionManagerImpl

runrmflights : server
	java -classpath $(PROJECTROOT) -DrmiPort=5444 -DrmiName=RMFlights -Djava.security.policy=./security-policy transaction.rm.ResourceManagerImpl

runrmrooms : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMRooms -Djava.security.policy=./security-policy transaction.rm.ResourceManagerImpl

runrmcars : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCars -Djava.security.policy=./security-policy transaction.rm.ResourceManagerImpl

runrmcustomers : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCustomers -Djava.security.policy=./security-policy transaction.rm.ResourceManagerImpl

runwc : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl

runclient : client
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) transaction.Client

clean :
	$(RM) *.class
	$(RM) ./exception/*.class
	$(RM) ./model/*.class
	$(RM) ./rm/*.class

%.class : %.java
	javac -classpath $(PROJECTROOT) $<

%_Stub.class : %.java
	rmic -classpath $(PROJECTROOT) -d $(PROJECTROOT) transaction.$*

.PHONY : runregistry runtm runrmflights runrmrooms runrmcars runrmcustomers runwc runclient clean

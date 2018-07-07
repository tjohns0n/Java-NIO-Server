.PHONY: all
all: scaling
debug:
	javac cs455/Debug.java
scaling: debug
	javac \
	cs455/scaling/server/*.java \
	cs455/scaling/client/*.java \
	cs455/scaling/message/*.java \
	cs455/scaling/pool/*.java \
	cs455/scaling/utils/*.java \
	cs455/scaling/work/*.java
overlay: debug
	javac \
	cs455/overlay/messages/*.java \
	cs455/overlay/node/*.java \
	cs455/overlay/nodes/*.java \
	cs455/overlay/nodes/events/*.java \
	cs455/overlay/nodes/networking/*.java \
	cs455/overlay/nodes/registry/*.java \
	cs455/overlay/nodes/statistics/*.java
clean: cleanOverlay cleanScaling
	rm -f cs455/*.class
cleanScaling:
	rm -f \
	cs455/scaling/server/*.class \
	cs455/scaling/client/*.class \
	cs455/scaling/message/*.class \
	cs455/scaling/pool/*.class \
	cs455/scaling/utils/*.class \
	cs455/scaling/work/*.class
cleanOverlay:
	rm -f cs455/*.class \
	cs455/overlay/messages/*.class \
	cs455/overlay/node/*.class \
	cs455/overlay/nodes/*.class \
	cs455/overlay/nodes/events/*.class \
	cs455/overlay/nodes/networking/*.class \
	cs455/overlay/nodes/registry/*.class \
	cs455/overlay/nodes/statistics/*.class
tar:
	tar -cf Johnson_Tanner_HW2-PC.tar Makefile README cs455/scaling cs455/Debug.java

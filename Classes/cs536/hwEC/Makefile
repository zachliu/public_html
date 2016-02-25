OBJS = cs536_hwEC.o
CC = g++
DEBUG = -g
CFLAGS = -Wall -c $(DEBUG)
LFLAGS = -Wall $(DEBUG)

p1 : $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o cs536_hwEC

cs536_hwEC.o : cs536_hwEC.cpp
	$(CC) $(CFLAGS) cs536_hwEC.cpp

clean:
	\rm *.o *~ p1
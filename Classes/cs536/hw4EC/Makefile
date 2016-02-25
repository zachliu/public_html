OBJS = cs536_hw4EC.o
CC = g++
DEBUG = -g
CFLAGS = -Wall -c $(DEBUG)
LFLAGS = -Wall $(DEBUG)

p1 : $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o cs536_hw4EC

cs536_hw4EC.o : cs536_hw4EC.cpp
	$(CC) $(CFLAGS) cs536_hw4EC.cpp

clean:
	\rm *.o *~ p1
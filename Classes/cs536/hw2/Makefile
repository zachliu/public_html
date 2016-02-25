OBJS = cs536_hw2.o
CC = g++
DEBUG = -g
CFLAGS = -Wall -c $(DEBUG)
LFLAGS = -Wall $(DEBUG)

p1 : $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o cs536_hw2

cs536_hw2.o : cs536_hw2.cpp
	$(CC) $(CFLAGS) cs536_hw2.cpp

clean:
	\rm *.o *~ p1
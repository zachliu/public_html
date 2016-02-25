OBJS = cs536_hw1.o
CC = g++
DEBUG = -g
CFLAGS = -Wall -c $(DEBUG)
LFLAGS = -Wall $(DEBUG)

p1 : $(OBJS)
	$(CC) $(LFLAGS) $(OBJS) -o cs536_hw1

cs536_hw1.o : cs536_hw1.cpp
	$(CC) $(CFLAGS) cs536_hw1.cpp

clean:
	\rm *.o *~ p1
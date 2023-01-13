/*
 * This file illustrates linkedlist.pde (an important data structure in my project) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/**
 * This is an auxiliary clss represents nodes for my linkedlist.
 */
class Node {
  /** an int array to store x/y coordinates */
  int[] xy;

  /** an int array to store the RGB values of this node */
  int[] rgb;

  /** next node of this node */
  Node next;

  /** previous node of this node */
  Node previous;


  /**
   * The constructor nitializes all variables.
   */
  public Node(int[] xy, int r, int g, int b) {
    this.xy = xy;
    this.rgb = new int[]{r, g, b};
    this.next = null;
    this.previous = null;
  }
}

/**
 * This is an auxiliary class represents linkedlist used in this program.
 * Although I name it linkedlist, it is slightly different from the usual
 * linkedlist => the previous node of the first node and the next node of
 * the last node are not null but the last node and the first node respectively,
 */
class LinkedList {
  /** the pointer to the first node */
  Node first;

  /** the pointer to the last node */
  Node last;

  /** the size of the linkedlist */
  int length;

  /**
   * The constructor initializes all variables.
   */
  public LinkedList() {
    first = null;
    last = null;
    length = 0;
  }

  /**
   * This function adds a node to the front of the linkedlist.
   */
  public void addFirst(int[] temp, int r, int g, int b) {
    Node n = new Node(temp, r, g, b);
    if (length == 0) {
      first = n;
      last = first;
      first.next = first;
      first.previous = first;
    } else {
      n.next = first;
      n.previous = last;
      first.previous = n;
      last.next = n;
      first = n;
    }
    length++;
  }

  /**
   * This function adds a node to the end of the linkedlist.
   */
  public void addLast(int[] temp, int r, int g, int b) {
    Node n = new Node(temp, r, g, b);
    if (length == 0) {
      last = n;
      first = last;
      last.next = last;
      last.previous = last;
    } else {
      n.previous = last;
      n.next = first;
      last.next = n;
      first.previous = n;
      last = n;
    }
    length++;
  }

  /**
   * This function clear the linkedlist.
   */
  void clear() {
    first = null;
    last = null;
    length = 0;
  }

  /**
   * This function returns the x/y coordinates of the first node.
   */
  int[] getFirst() {
    return first.xy;
  }

  /**
   * This function removes the last node of the linkedlist and returns its x/y coordinates.
   */
  public int[] removeLast() {
    int[] temp = null;
    if (length > 1) {
      temp = last.xy;
      last = last.previous;
      last.next = first;
      first.previous = last;
      length--;
      return temp;
    } else if (length == 1) {
      temp = last.xy;
      this.clear();
      return temp;
    } else {
      System.exit(-1);
      return temp;
    }
  }

  /**
   * This function removes a single node from the linkedlist and returns the previous node of the removed node.
   * It does not check if the node to be removed exists in the list because it is checked in the algorithm that
   * calls this function.
   */
  public void remove(Node node) {
    if (length > 1) {
      if (node == first) {
        first = first.next;
        first.previous = last;
        last.next = first;
      } else if (node == last) {
        last = last.previous;
        last.next = first;
        first.previous = last;
      } else {
        node.previous.next = node.next;
        node.next.previous = node.previous;
      }
      length--;
    } else this.clear();
  }

  /**
   * This function adds a single node right after a node in the linkedlist.
   */
  public void insert(Node n1, Node n2) {
    if (n1 == last) {
      last = n2;
      n2.previous = n1;
      n1.next = n2;
      n2.next = first;
      first.previous = n2;
    } else {
      n2.previous = n1;
      n2.next = n1.next;
      n2.next.previous = n2;
      n1.next = n2;
    }
    length++;
  }

  /**
   * This function returns the size of the linkedlist.
   */
  public int size() {
    return length;
  }

  /**
   * This function returns the first node of the linkedlist.
   */
  public Node getPointer() {
    return first;
  }
}

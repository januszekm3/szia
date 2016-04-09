package model.crossroad;

public class Edge {
    private Node begin;
    private Node end;

    public Edge(Node begin, Node end) {
        this.begin = begin;
        this.end = end;
    }

    public Node getBegin() {
        return begin;
    }

    public Node getEnd() {
        return end;
    }

    public float length() {
        int dx = end.getPosition().getX() - begin.getPosition().getX();
        int dy = end.getPosition().getY() - begin.getPosition().getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((begin == null) ? 0 : begin.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (begin == null) {
            if (other.begin != null)
                return false;
        } else if (!begin.equals(other.begin))
            return false;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        return true;
    }


}

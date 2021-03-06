package lesson19.home.operations;

import lesson19.home.AbstractNode;
import lesson19.home.Node;

import java.util.List;
import java.util.Map;

public class PlusNode extends AbstractNode {
    public PlusNode() {
    }

    public PlusNode(Node left, Node right) {
        super(left, right);
    }

    @Override
    public double getResult(Map<String, Double> values) {
        super.getResult(values);
        double result = 0;
        List<Node> childes = getChildes();
        if (childes.isEmpty()) {
            result = leftNode.getResult(values) + rightNode.getResult(values);
        } else {
            for (Node node : childes) {
                result += node.getResult(values);
            }
        }
        return result;
    }
}

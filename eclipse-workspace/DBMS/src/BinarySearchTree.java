
public class BinarySearchTree {
	private BSTNode root;
	
	public void insert(int key, long filePosition) {
		root = insertRec(root, key, filePosition);
	}
	
	public BSTNode insertRec(BSTNode root, int key, long filePosition) {
		if (root == null) return new BSTNode(key, filePosition);
		
		if (key < root.key) {
			root.left = insertRec(root.left, key, filePosition);
		} else if (key > root.key) {
			root.right = insertRec(root.right, key, filePosition);
		}
		
		return root;
	}
	
	public Long search(int key) {
		return searchRec(root, key);
	}
	
	public Long searchRec(BSTNode root, int key) {
		if (root == null) return null;
		if (key == root.key) return root.filePosition;
		if (key < root.key) return searchRec(root.left, key);
		return searchRec(root.right, key);
	}
}

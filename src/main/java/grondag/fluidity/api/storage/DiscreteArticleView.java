package grondag.fluidity.api.storage;

public interface DiscreteArticleView<V extends DiscreteArticleView<V>> extends ArticleView {
    long count();
}

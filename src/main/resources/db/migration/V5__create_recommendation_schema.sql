CREATE TABLE recent_views (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_recent_views_user_viewed ON recent_views (user_id, viewed_at DESC);

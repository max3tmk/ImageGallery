import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchAllImages } from "../features/imageSlice";
import { getUserIdFromToken, getUsernameFromToken } from "../utils/jwtUtils";
import api from "../api/axios.js";
import likeIcon from "../assets/icons/like.svg";
import commentIcon from "../assets/icons/comment.svg";
import editIcon from "../assets/icons/edit.svg";
import deleteIcon from "../assets/icons/delete.svg";

export default function ImageGallery() {
    const dispatch = useDispatch();
    const { all, status, error } = useSelector((state) => state.images);
    const accessToken = useSelector((state) => state.auth.token);
    const userId = accessToken ? getUserIdFromToken(accessToken) : null;
    const username = accessToken ? getUsernameFromToken(accessToken) : "User";

    const [page, setPage] = useState(0);
    const [likes, setLikes] = useState({});
    const [comments, setComments] = useState({});
    const [newComment, setNewComment] = useState({});
    const [showCommentInput, setShowCommentInput] = useState({});
    const [editingComment, setEditingComment] = useState({});
    const [editContent, setEditContent] = useState({});

    /**
     * @typedef {Object} Comment
     * @property {string} id
     * @property {string} userId
     * @property {string} authorName
     * @property {string} content
     */

    useEffect(() => {
        dispatch(fetchAllImages({ page, size: 10 }));
    }, [dispatch, page]);

    useEffect(() => {
        if (!all?.content) return;

        const loadInteractions = async () => {
            const likesUpdates = {};
            const commentsUpdates = {};

            for (const img of all.content) {
                try {
                    const res = await api.get(`/images/${img.id}/likes/count`, {
                        headers: { Authorization: `Bearer ${accessToken}` },
                    });
                    likesUpdates[img.id] = res.data.likesCount ?? 0;
                } catch (e) {
                    console.error("Error loading likes:", e);
                }

                try {
                    const res = await api.get(`/images/${img.id}/comments`, {
                        headers: { Authorization: `Bearer ${accessToken}` },
                    });
                    commentsUpdates[img.id] = res.data;
                } catch (e) {
                    console.error("Error loading comments:", e);
                }
            }

            setLikes(likesUpdates);
            setComments(commentsUpdates);
        };

        loadInteractions();
    }, [all, accessToken]);

    const handleLikeClick = async (imageId) => {
        try {
            await api.post(`/images/${imageId}/likes`, null, {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            const res = await api.get(`/images/${imageId}/likes/count`, {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            setLikes((prev) => ({ ...prev, [imageId]: res.data.likesCount }));
        } catch (e) {
            console.error("Toggle like error:", e);
        }
    };

    const handleCommentIconClick = (imageId) => {
        setShowCommentInput((prev) => ({
            ...prev,
            [imageId]: !prev[imageId],
        }));
    };

    const handleCommentSubmit = async (imageId) => {
        const content = newComment[imageId]?.trim();
        if (!content) return;

        try {
            const response = await api.post(
                `/images/${imageId}/comments`,
                { content },
                { headers: { Authorization: `Bearer ${accessToken}` } }
            );

            const newCommentObj = {
                ...response.data,
                userId: userId,
            };

            setComments((prev) => ({
                ...prev,
                [imageId]: [newCommentObj, ...(prev[imageId] || [])],
            }));

            setNewComment((prev) => ({ ...prev, [imageId]: "" }));
            setShowCommentInput((prev) => ({ ...prev, [imageId]: false }));
        } catch (e) {
            console.error("Add comment error:", e);
        }
    };

    const startEditing = (commentId, currentContent) => {
        setEditingComment((prev) => ({ ...prev, [commentId]: true }));
        setEditContent((prev) => ({ ...prev, [commentId]: currentContent }));
    };

    const saveEdit = async (imageId, commentId) => {
        const content = editContent[commentId]?.trim();
        if (!content) return;

        try {
            const response = await api.put(
                `/images/${imageId}/comments/${commentId}`,
                { content },
                { headers: { Authorization: `Bearer ${accessToken}` } }
            );

            setComments((prev) => {
                const imageComments = [...(prev[imageId] || [])];
                const index = imageComments.findIndex(c => c.id === commentId);
                if (index !== -1) {
                    imageComments[index] = { ...imageComments[index], content: response.data.content };
                }
                return { ...prev, [imageId]: imageComments };
            });

            setEditingComment((prev) => ({ ...prev, [commentId]: false }));
        } catch (e) {
            console.error("Update comment error:", e);
        }
    };

    const handleDeleteComment = async (imageId, commentId) => {
        if (!window.confirm("Delete comment?")) return;

        try {
            await api.delete(`/images/${imageId}/comments/${commentId}`, {
                headers: { Authorization: `Bearer ${accessToken}` },
            });

            setComments((prev) => {
                const imageComments = [...(prev[imageId] || [])];
                const filtered = imageComments.filter(c => c.id !== commentId);
                return { ...prev, [imageId]: filtered };
            });
        } catch (e) {
            console.error("Delete comment error:", e);
        }
    };

    if (status === "loading") return <p>Loading...</p>;
    if (status === "failed") {
        const errText = typeof error === "string" ? error : JSON.stringify(error, null, 2);
        return <pre style={{ color: "red" }}>Error: {errText}</pre>;
    }

    return (
        <div>
            <h2>All Images</h2>
            <div style={{ display: "flex", flexWrap: "wrap" }}>
                {all?.content?.map((image) => (
                    <div key={image.id} style={{ margin: "10px", width: "200px" }}>
                        <p style={{ fontWeight: "bold", marginBottom: "4px" }}>
                            {image.description || "No description"}
                        </p>
                        <img
                            src={image.url}
                            alt={image.description || "Image"}
                            onClick={() => handleLikeClick(image.id)}
                            style={{
                                width: "100%",
                                height: "150px",
                                objectFit: "cover",
                                cursor: "pointer",
                            }}
                        />

                        <div style={{ marginTop: "6px", display: "flex", alignItems: "center", gap: "8px" }}>
                            {likes[image.id] > 0 && (
                                <div style={{ display: "flex", alignItems: "center", gap: "4px", marginTop: "6px" }}>
                                    <img
                                        src={likeIcon}
                                        alt="like"
                                        style={{ width: "20px", height: "20px" }}
                                    />
                                    <span>{likes[image.id]}</span>
                                </div>
                            )}

                            <img
                                src={commentIcon}
                                alt="comment"
                                style={{ width: "20px", height: "20px", cursor: "pointer" }}
                                onClick={() => handleCommentIconClick(image.id)}
                            />
                        </div>

                        {showCommentInput[image.id] && (
                            <input
                                type="text"
                                value={newComment[image.id] || ""}
                                onChange={(e) =>
                                    setNewComment((prev) => ({
                                        ...prev,
                                        [image.id]: e.target.value,
                                    }))
                                }
                                onKeyDown={(e) => {
                                    if (e.key === "Enter") handleCommentSubmit(image.id);
                                }}
                                onBlur={() => {
                                    if (newComment[image.id]?.trim()) {
                                        handleCommentSubmit(image.id);
                                    } else {
                                        setShowCommentInput(prev => ({ ...prev, [image.id]: false }));
                                    }
                                }}
                                placeholder="Comment..."
                                style={{
                                    marginTop: "6px",
                                    width: "100%",
                                    padding: "4px",
                                    fontSize: "14px",
                                }}
                                autoFocus
                            />
                        )}

                        {comments[image.id]?.length > 0 && (
                            <div style={{ marginTop: "6px", fontSize: "13px", maxHeight: "100px", overflowY: "auto" }}>
                                {comments[image.id].map(/** @type {Comment} */ (comment) => (
                                    <div key={comment.id} style={{ marginBottom: "6px", display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                                        {editingComment[comment.id] ? (
                                            <div style={{
                                                padding: "4px",
                                                border: "1px solid #ccc",
                                                borderRadius: "4px",
                                                backgroundColor: "#fafafa",
                                                width: "100%"
                                            }}>
                                                <input
                                                    type="text"
                                                    value={editContent[comment.id] || ""}
                                                    onChange={(e) => setEditContent(prev => ({ ...prev, [comment.id]: e.target.value }))}
                                                    onKeyDown={(e) => {
                                                        if (e.key === "Enter") saveEdit(image.id, comment.id);
                                                    }}
                                                    onBlur={() => saveEdit(image.id, comment.id)}
                                                    autoFocus
                                                    style={{
                                                        width: "100%",
                                                        padding: "2px",
                                                        fontSize: "13px",
                                                        border: "none",
                                                        outline: "none",
                                                        background: "transparent"
                                                    }}
                                                />
                                            </div>
                                        ) : (
                                            <>
                                                <div>
                                                    <strong>{comment.authorName || "User"}:</strong> {comment.content}
                                                </div>
                                                {comment.userId === userId && (
                                                    <div style={{ display: "flex", gap: "4px" }}>
                                                        <img
                                                            src={editIcon}
                                                            alt="edit"
                                                            style={{ width: "16px", height: "16px", cursor: "pointer" }}
                                                            onClick={() => startEditing(comment.id, comment.content)}
                                                        />
                                                        <img
                                                            src={deleteIcon}
                                                            alt="delete"
                                                            style={{ width: "16px", height: "16px", cursor: "pointer" }}
                                                            onClick={() => handleDeleteComment(image.id, comment.id)}
                                                        />
                                                    </div>
                                                )}
                                            </>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {all?.totalElements > 0 ? (
                <div style={{ marginTop: "20px", textAlign: "center" }}>
                    <button disabled={page === 0} onClick={() => setPage(page - 1)}>
                        Prev
                    </button>
                    <span style={{ margin: "0 10px" }}>
                        Page {page + 1} of {all.totalPages}
                    </span>
                    <button
                        disabled={page >= all.totalPages - 1}
                        onClick={() => setPage(page + 1)}
                    >
                        Next
                    </button>
                </div>
            ) : (
                <div style={{ marginTop: "20px", textAlign: "center", fontSize: "18px", color: "#666" }}>
                    No images found.
                    <br />
                    Try uploading one!
                </div>
            )}
        </div>
    );
}